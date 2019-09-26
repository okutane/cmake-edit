import * as path from 'path';
import * as vscode from 'vscode';

import {
	LanguageClient,
	LanguageClientOptions,
	ServerOptions,
	TransportKind
} from 'vscode-languageclient';

let client: LanguageClient;

export function activate(context: vscode.ExtensionContext) {
	let java = "/Library/Java/JavaVirtualMachines/jdk1.8.0_172.jdk/Contents/Home/bin/java";
	let debugAgent = "-agentlib:jdwp=transport=dt_socket,server=n,address=ntb-cerbat.net.billing.ru:5005,suspend=y";
	let serverOptions: ServerOptions = {
		run: { command: java, args: ["-jar a.jar"] },
		debug: {
			command: java, args: ["-jar", "/Users/dmitry.matveyev/okutane/cmake-edit/cmake-lsp-server/target/cmake-lsp-server-1.0-SNAPSHOT-jar-with-dependencies.jar"]
		}
	};

	// Options to control the language client
	let clientOptions: LanguageClientOptions = {
		// Register the server for plain text documents
		documentSelector: [{ scheme: 'file', language: 'cmake' }],
		synchronize: {
			// Notify the server about file changes to '.clientrc files contained in the workspace
			fileEvents: vscode.workspace.createFileSystemWatcher('**/.clientrc')
		}
	};

	// Create the language client and start the client.
	client = new LanguageClient(
		'cmake',
		'CMake',
		serverOptions,
		clientOptions
	);

	// Start the client. This will also launch the server
	client.start();
}

// this method is called when your extension is deactivated
export function deactivate() { }
