package ru.urururu.cmakeedit.lsp;

import org.eclipse.lsp4j.Color;
import org.eclipse.lsp4j.ColorInformation;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentColorParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TextDocumentSyncOptions;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import ru.urururu.cmakeedit.core.ArgumentNode;
import ru.urururu.cmakeedit.core.CommandInvocationNode;
import ru.urururu.cmakeedit.core.CommentNode;
import ru.urururu.cmakeedit.core.ConstantNode;
import ru.urururu.cmakeedit.core.ExpressionNode;
import ru.urururu.cmakeedit.core.FileNode;
import ru.urururu.cmakeedit.core.Node;
import ru.urururu.cmakeedit.core.NodeVisitor;
import ru.urururu.cmakeedit.core.ParseErrorNode;
import ru.urururu.cmakeedit.core.SourceRef;
import ru.urururu.cmakeedit.core.parser.Parser;
import ru.urururu.cmakeedit.core.parser.StringParseContext;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Server {
    public static void main(String... args) {
        TextDocumentService textDocumentService = new TextDocumentService() {
            @Override
            public void didOpen(DidOpenTextDocumentParams didOpenTextDocumentParams) {
                throw new IllegalStateException(didOpenTextDocumentParams.toString());
            }

            @Override
            public void didChange(DidChangeTextDocumentParams didChangeTextDocumentParams) {

            }

            @Override
            public void didClose(DidCloseTextDocumentParams didCloseTextDocumentParams) {

            }

            @Override
            public void didSave(DidSaveTextDocumentParams didSaveTextDocumentParams) {

            }

            @Override
            public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(DocumentSymbolParams params) {
                return null;
            }

            @Override
            public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams position) {
                CompletableFuture<List<? extends DocumentHighlight>> cf = new CompletableFuture<>();

                // todo return all function calls to function at position (if any)

                cf.complete(null);

                return cf;
            }

            @Override
            public CompletableFuture<List<ColorInformation>> documentColor(DocumentColorParams params) {
                final CompletableFuture<List<ColorInformation>> result = new CompletableFuture<>();

                try {
                    URI uri = new URI(params.getTextDocument().getUri());
                    File file = new File(uri);

                    String content = new String(Files.readAllBytes(file.toPath()));
                    FileNode fileNode = Parser.parse(new StringParseContext(content, 0));

                    List<ColorInformation> colors = new ArrayList<>();

                    fileNode.visitAll(new NodeVisitor() {
                        @Override
                        public void accept(ArgumentNode node) {
                            colors.add(new ColorInformation(toRange(node), new Color(0.5, 0.5, 0.5, 1)));
                        }

                        @Override
                        public void accept(CommentNode node) {
                            colors.add(new ColorInformation(toRange(node), new Color(0, 0.5, 0, 1)));
                        }

                        @Override
                        public void accept(ExpressionNode node) {
                            colors.add(new ColorInformation(toRange(node), new Color(0, 0, 0.5, 1)));
                        }

                        @Override
                        public void accept(CommandInvocationNode node) {
                            colors.add(new ColorInformation(toRange(node), new Color(0.5, 0.5, 0, 1)));
                        }

                        @Override
                        public void accept(ParseErrorNode node) {
                            colors.add(new ColorInformation(toRange(node), new Color(0.5, 0, 0, 1)));
                        }

                        @Override
                        public void accept(ConstantNode node) {
                            colors.add(new ColorInformation(toRange(node), new Color(0, 0.5, 0.5, 1)));
                        }

                        private Range toRange(Node node) {
                            return new Range(toPosition(node.getStart()), toPosition(node.getEnd()));
                        }

                        private Position toPosition(SourceRef sourceRef) {
                            return new Position(1 + colors.size(), 1 + sourceRef.getOffset());
                        }
                    });

                    result.complete(colors.subList(0, 1));
                } catch (Exception e) {
                    result.completeExceptionally(e);
                }

                return result;
            }
        };

        WorkspaceService workspaceService = new WorkspaceService() {
            @Override
            public void didChangeConfiguration(DidChangeConfigurationParams didChangeConfigurationParams) {

            }

            @Override
            public void didChangeWatchedFiles(DidChangeWatchedFilesParams didChangeWatchedFilesParams) {

            }
        };

        LanguageServer server = new CMakeLanguageServer(textDocumentService, workspaceService);

        Launcher<LanguageClient> launcher =
                LSPLauncher.createServerLauncher(server,
                        System.in,
                        System.out);

        launcher.startListening();
    }

    private static class CMakeLanguageServer implements LanguageServer, LanguageClientAware {
        private final TextDocumentService textDocumentService;
        private final WorkspaceService workspaceService;

        public CMakeLanguageServer(TextDocumentService textDocumentService, WorkspaceService workspaceService) {
            this.textDocumentService = textDocumentService;
            this.workspaceService = workspaceService;
        }

        @Override
        public CompletableFuture<InitializeResult> initialize(InitializeParams initializeParams) {
            ServerCapabilities serverCapabilities = new ServerCapabilities();
            serverCapabilities.setColorProvider(false);
            serverCapabilities.setDocumentHighlightProvider(true);
//            serverCapabilities.setDocumentFormattingProvider(true);
//            serverCapabilities.setDocumentHighlightProvider(true);
//            TextDocumentSyncOptions textDocumentSync = new TextDocumentSyncOptions();
//            textDocumentSync.setOpenClose(true);
//            textDocumentSync.setChange(TextDocumentSyncKind.Full);
//            serverCapabilities.setTextDocumentSync(textDocumentSync);
//
//            serverCapabilities.setCompletionProvider(new CompletionOptions(true, null));

            CompletableFuture<InitializeResult> result = new CompletableFuture<>();
            result.complete(new InitializeResult(serverCapabilities));

            return result;
        }

        @Override
        public CompletableFuture<Object> shutdown() {
            return null;
        }

        @Override
        public void exit() {

        }

        @Override
        public TextDocumentService getTextDocumentService() {
            return textDocumentService;
        }

        @Override
        public WorkspaceService getWorkspaceService() {
            return workspaceService;
        }

        @Override
        public void connect(LanguageClient languageClient) {
            languageClient.showMessage(new MessageParams(MessageType.Info, "connected"));
        }
    }
}
