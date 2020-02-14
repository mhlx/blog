package me.qyh.blog.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.BytesRef;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.util.CollectionUtils;

import me.qyh.blog.entity.Article;
import me.qyh.blog.entity.Category;
import me.qyh.blog.entity.Tag;
import me.qyh.blog.utils.FileUtils;
import me.qyh.blog.utils.TimeUtils;
import me.qyh.blog.vo.HandledArticleQueryParam;
import me.qyh.blog.vo.PageResult;

public class ArticleIndexer {

	protected static final String TITLE = "title";
	protected static final String CONTENT = "content";
	protected static final String ID = "id";
	protected static final String ALIAS = "alias";
	protected static final String DATE = "date";
	protected static final String CATEGORY = "category";
	protected static final String SUMMARY = "summary";
	protected static final String TAG = "tag";

	private static final String PWD = "password";
	private static final String PRIVATE = "private";

	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	protected static final Path p = Paths.get(System.getProperty("user.home")).resolve("blog/index");

	static {
		try {
			FileUtils.forceMkdir(p);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private final IndexWriter indexWriter;
	private final Directory directory;
	private final Map<String, Float> boosts;
	private SearcherManager searcherManager;

	public ArticleIndexer() throws IOException {
		IndexWriterConfig config = new IndexWriterConfig(createAnalyzer());
		this.directory = createDirectory();
		this.indexWriter = new IndexWriter(directory, config);
		this.boosts = Map.copyOf(createBoosts());
		this.searcherManager = new SearcherManager(indexWriter, null);
	}

	public void addDocument(Article... articles) throws IOException {
		indexWriter.addDocuments(Arrays.stream(articles).map(this::buildDocument).collect(Collectors.toList()));
		searcherManager.maybeRefresh();
	}

	public void deleteDocument(int... ids) throws IOException {
		Builder builder = new Builder();
		for (int id : ids) {
			builder.add(new TermQuery(new Term(ID, String.valueOf(id))), Occur.SHOULD);
		}
		indexWriter.deleteDocuments(builder.build());
		searcherManager.maybeRefresh();
	}

	public void updateDocument(Article... articles) throws IOException {
		for (Article article : articles) {
			Document document = buildDocument(article);
			indexWriter.updateDocument(new Term(ID, String.valueOf(article.getId())), document);
		}
		searcherManager.maybeRefresh();
	}

	public void rebuild(List<Article> articles) throws IOException {
		indexWriter.deleteAll();
		indexWriter.addDocuments(articles.stream().map(this::buildDocument).collect(Collectors.toList()));
		indexWriter.commit();
		searcherManager.maybeRefreshBlocking();
	}

	public PageResult<Integer> query(HandledArticleQueryParam param) throws IOException {

		Query query;
		try {
			query = buildQuery(param);
		} catch (ParseException e) {
			return new PageResult<Integer>(param, 0, List.of());
		}

		IndexSearcher searcher = searcherManager.acquire();
		try {
			int limit = param.getPageSize();
			int offset = param.getOffset();
			int maxResults = limit + offset;
			TopDocs docs = searcher.search(query, maxResults);
			long totalHits = docs.totalHits.value;
			List<Integer> ids = new ArrayList<>();
			for (int i = 0, len = docs.scoreDocs.length; i < limit && i + offset < totalHits && i + offset < len; i++) {
				ScoreDoc sd = docs.scoreDocs[i + offset];
				Document doc = searcher.doc(sd.doc);
				ids.add(Integer.parseInt(doc.get(ID)));
			}
			return new PageResult<Integer>(param, (int) totalHits, ids);
		} finally {
			if (searcher != null) {
				searcherManager.release(searcher);
				searcher = null;
			}
		}
	}

	protected Query buildQuery(HandledArticleQueryParam param) throws ParseException {
		MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[] { TITLE, SUMMARY, CONTENT, ALIAS },
				createAnalyzer(), this.boosts);

		Query query = parser.parse(MultiFieldQueryParser.escape(param.getQuery()));

		Builder builder = new Builder();
		builder.add(query, Occur.MUST);

		if (!param.isQueryPasswordProtected()) {
			builder.add(new TermQuery(new Term(PWD, "false")), Occur.MUST);
		}
		if (!param.isQueryPrivate()) {
			builder.add(new TermQuery(new Term(PRIVATE, "false")), Occur.MUST);
		}

		if (param.getBegin() != null || param.getEnd() != null) {
			BytesRef lower = null, upper = null;
			if (param.getBegin() != null) {
				lower = new BytesRef(TimeUtils.format(param.getBegin(), DATE_FORMAT));
			}
			if (param.getEnd() != null) {
				upper = new BytesRef(TimeUtils.format(param.getEnd(), DATE_FORMAT));
			}
			builder.add(new TermRangeQuery(DATE, lower, upper, true, true), Occur.MUST);
		}

		if (param.getCategoryId() != null) {
			builder.add(new TermQuery(new Term(CATEGORY, String.valueOf(param.getCategoryId()))), Occur.MUST);
		}
		if (param.getTagId() != null) {
			builder.add(new TermQuery(new Term(TAG, String.valueOf(param.getTagId()))), Occur.MUST);
		}

		return builder.build();
	}

	protected Directory createDirectory() throws IOException {
		return new MMapDirectory(p);
	}

	protected Analyzer createAnalyzer() {
		return new SmartChineseAnalyzer();
	}

	private Document buildDocument(Article article) {
		Document document = doBuildDocument(article);
		document.add(new StringField(PWD, String.valueOf(article.isHasPassword()), Field.Store.NO));
		document.add(new StringField(PRIVATE, article.getIsPrivate().toString(), Field.Store.NO));
		return document;
	}

	protected Document doBuildDocument(Article article) {
		Document document = new Document();
		document.add(new StringField(ID, String.valueOf(article.getId()), Field.Store.YES));

		String summary = article.getSummary();
		if (summary != null) {
			document.add(new TextField(CONTENT, getPlainContent(summary), Field.Store.NO));
		}

		document.add(new TextField(CONTENT, getPlainContent(article.getContent()), Field.Store.NO));
		document.add(new TextField(TITLE, article.getTitle(), Field.Store.NO));
		document.add(
				new StringField(DATE, TimeUtils.format(article.getPubDate(), "yyyy-MM-dd HH:mm:ss"), Field.Store.YES));
		if (article.getAlias() != null) {
			document.add(new TextField(ALIAS, article.getAlias(), Field.Store.NO));
		}

		Set<Category> categories = article.getCategories();
		if (!CollectionUtils.isEmpty(categories)) {
			for (Category category : categories) {
				document.add(new StringField(CATEGORY, String.valueOf(category.getId()), Store.NO));
			}
		}
		Set<Tag> tags = article.getTags();
		if (!CollectionUtils.isEmpty(tags)) {
			for (Tag tag : tags) {
				document.add(new StringField(TAG, String.valueOf(tag.getId()), Store.NO));
			}
		}
		return document;
	}

	protected String getPlainContent(String content) {
		return Jsoup.clean(content, Whitelist.none());
	}

	public void close() throws IOException {
		searcherManager.close();
		indexWriter.close();
	}

	protected Map<String, Float> createBoosts() {
		Map<String, Float> boosts = new HashMap<>();
		boosts.put(TITLE, 10F);
		boosts.put(ALIAS, 7F);
		boosts.put(SUMMARY, 4F);
		boosts.put(CONTENT, 3F);
		return boosts;
	}
}
