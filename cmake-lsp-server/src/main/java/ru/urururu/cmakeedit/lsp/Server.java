package ru.urururu.cmakeedit.lsp;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TextDocumentSyncOptions;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.Collections;
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
            serverCapabilities.setColorProvider(true);
            serverCapabilities.setDocumentFormattingProvider(true);
            serverCapabilities.setDocumentHighlightProvider(true);
            TextDocumentSyncOptions textDocumentSync = new TextDocumentSyncOptions();
            textDocumentSync.setOpenClose(true);
            textDocumentSync.setChange(TextDocumentSyncKind.Full);
            serverCapabilities.setTextDocumentSync(textDocumentSync);

            serverCapabilities.setCompletionProvider(new CompletionOptions(true, null));

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
