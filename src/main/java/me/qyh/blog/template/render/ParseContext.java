/*
 * Copyright 2016 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.qyh.blog.template.render;

import java.util.List;
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
