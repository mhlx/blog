package me.qyh.blog.template.render;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.transaction.TransactionStatus;

/**
 * 解析上下文
 * 
 * @author mhlx
 *
 */
public class ParseContext {

	private TransactionStatus transactionStatus;
	private ParseConfig config;
	private ParsedTemplate root;
	private final Map<String, Map<String, String>> namedRenderHandlers = new LinkedHashMap<>();

	ParseContext() {
		super();
	}

	public TransactionStatus getTransactionStatus() {
		return transactionStatus;
	}

	public void setTransactionStatus(TransactionStatus transactionStatus) {
		this.transactionStatus = transactionStatus;
	}

	public ParseConfig getConfig() {
		return config;
	}

	public void setConfig(ParseConfig config) {
		this.config = config;
	}

	public boolean isOnlyCallable() {
		return config.isOnlyCallable();
	}

	public Optional<ParsedTemplate> getRoot() {
		return Optional.ofNullable(root);
	}

	public void setRoot(ParsedTemplate root) {
		this.root = root;
	}

	public Map<String, Map<String, String>> getNamedRenderHandlers() {
		return namedRenderHandlers;
	}

	public Optional<ParsedTemplate> getLastChain() {
		if (root == null) {
			return Optional.empty();
		}
		ParsedTemplate chainRoot = new ParsedTemplate(root);
		addToChain(this.root, chainRoot);
		return Optional.of(chainRoot);
	}

	private void addToChain(ParsedTemplate root, ParsedTemplate chainRoot) {
		List<ParsedTemplate> children = root.getChildren();
		if (!children.isEmpty()) {
			ParsedTemplate lastChild = children.get(children.size() - 1);
			ParsedTemplate child = new ParsedTemplate(lastChild);
			chainRoot.addChild(child);
			addToChain(lastChild, child);
		}
	}

}
