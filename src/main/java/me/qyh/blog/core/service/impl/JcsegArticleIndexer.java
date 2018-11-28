package me.qyh.blog.core.service.impl;

import java.io.IOException;

import org.lionsoul.jcseg.analyzer.JcsegAnalyzer;
import org.lionsoul.jcseg.tokenizer.core.ADictionary;
import org.lionsoul.jcseg.tokenizer.core.ILexicon;
import org.lionsoul.jcseg.tokenizer.core.IWord;
import org.lionsoul.jcseg.tokenizer.core.JcsegTaskConfig;

public class JcsegArticleIndexer extends ArticleIndexer {

	public enum JcsegMode {

		SIMPLE(1), COMPLEX(2), DECECT(3), SEARCH(4);

		private final int mode;

		JcsegMode(int mode) {
			this.mode = mode;
		}

		public int getMode() {
			return mode;
		}
	}

	public JcsegArticleIndexer(JcsegMode mode) throws IOException {
		super(new _Analyzer(mode));
	}

	@Override
	public void doRemoveTags(String... tags) {
		ADictionary dict = ((_Analyzer) analyzer).getDict();
		for (String tag : tags) {
			dict.remove(ILexicon.CJK_WORD, tag);
		}
	}

	@Override
	public void doAddTags(String... tags) {
		ADictionary dict = ((_Analyzer) analyzer).getDict();
		for (String tag : tags) {
			dict.add(ILexicon.CJK_WORD, tag, IWord.T_CJK_WORD);
		}
	}

	private static final class _Analyzer extends JcsegAnalyzer {

		_Analyzer(JcsegMode mode) {
			super(mode.mode);
			JcsegTaskConfig taskConfig = getTaskConfig();
			taskConfig.setClearStopwords(true);
			/**
			 * http://git.oschina.net/lionsoul/jcseg/issues/24
			 */
			taskConfig.setAppendCJKSyn(false);
			taskConfig.setLoadCJKPinyin(false);
			taskConfig.setLoadCJKSyn(false);
			taskConfig.setAppendCJKPinyin(false);
		}
	}

}
