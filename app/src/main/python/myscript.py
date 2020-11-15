import numpy as np
from collections import defaultdict
from scipy.sparse import csr_matrix
from sklearn.metrics import pairwise_distances


class KRWordRank:
    """Unsupervised Korean Keyword Extractor

    Implementation of Kim, H. J., Cho, S., & Kang, P. (2014). KR-WordRank:
    An Unsupervised Korean Word Extraction Method Based on WordRank.
    Journal of Korean Institute of Industrial Engineers, 40(1), 18-33.

    Arguments
    ---------
    min_count : int
        Minimum frequency of subwords used to construct subword graph
        Default is 5
    max_length : int
        Maximum length of subwords used to construct subword graph
        Default is 10
    verbose : Boolean
        If True, it shows training status
        Default is False

    Usage
    -----
        >>> from krwordrank.word import KRWordRank

        >>> texts = ['예시 문장 입니다', '여러 문장의 list of str 입니다', ... ]
        >>> wordrank_extractor = KRWordRank()
        >>> keywords, rank, graph = wordrank_extractor.extract(texts, beta, max_iter, verbose)
    """

    def __init__(self, min_count=5, max_length=10, verbose=False):
        self.min_count = min_count
        self.max_length = max_length
        self.verbose = verbose
        self.sum_weight = 1
        self.vocabulary = {}
        self.index2vocab = []

    def scan_vocabs(self, docs):
        """
        It scans subwords positioned of left-side (L) and right-side (R) of words.
        After scanning was done, KR-WordRank has index2vocab as class attribute.

        Arguments
        ---------
        docs : list of str
            Sentence list

        Returns
        -------
        counter : dict
            {(subword, 'L')] : frequency}
        """
        self.vocabulary = {}
        if self.verbose:
            print('scan vocabs ... ')

        counter = {}
        for doc in docs:

            for token in doc.split():
                len_token = len(token)
                counter[(token, 'L')] = counter.get((token, 'L'), 0) + 1

                for e in range(1, min(len(token), self.max_length)):
                    if (len_token - e) > self.max_length:
                        continue

                    l_sub = (token[:e], 'L')
                    r_sub = (token[e:], 'R')
                    counter[l_sub] = counter.get(l_sub, 0) + 1
                    counter[r_sub] = counter.get(r_sub, 0) + 1

        counter = {token: freq for token, freq in counter.items() if freq >= self.min_count}
        for token, _ in sorted(counter.items(), key=lambda x: x[1], reverse=True):
            self.vocabulary[token] = len(self.vocabulary)

        self._build_index2vocab()

        if self.verbose:
            print('num vocabs = %d' % len(counter))
        return counter

    def _build_index2vocab(self):
        self.index2vocab = [vocab for vocab, index in sorted(self.vocabulary.items(), key=lambda x: x[1])]
        self.sum_weight = len(self.index2vocab)

    def extract(self, docs, beta=0.85, max_iter=10, num_keywords=-1,
                num_rset=-1, vocabulary=None, bias=None, rset=None):
        """
        It constructs word graph and trains ranks of each node using HITS algorithm.
        After training it selects suitable subwords as words.

        Arguments
        ---------
        docs : list of str
            Sentence list.
        beta : float
            PageRank damping factor. 0 < beta < 1
            Default is 0.85
        max_iter : int
            Maximum number of iterations of HITS algorithm.
            Default is 10
        num_keywords : int
            Number of keywords sorted by rank.
            Default is -1. If the vaule is negative, it returns all extracted words.
        num_rset : int
            Number of R set words sorted by rank. It will be used to L-part word filtering.
            Default is -1.
        vocabulary : None or dict
            User specified vocabulary to index mapper
        bias : None or dict
            User specified HITS bias term
        rset : None or dict
            User specfied R set

        Returns
        -------
        keywords : dict
            word : rank dictionary. {str:float}
        rank : dict
            subword : rank dictionary. {int:float}
        graph : dict of dict
            Adjacent subword graph. {int:{int:float}}

        Usage
        -----
            >>> from krwordrank.word import KRWordRank

            >>> texts = ['예시 문장 입니다', '여러 문장의 list of str 입니다', ... ]
            >>> wordrank_extractor = KRWordRank()
            >>> keywords, rank, graph = wordrank_extractor.extract(texts, beta, max_iter, verbose)
        """

        rank, graph = self.train(docs, beta, max_iter, vocabulary, bias)

        lset = {self.int2token(idx)[0]: r for idx, r in rank.items() if self.int2token(idx)[1] == 'L'}
        if not rset:
            rset = {self.int2token(idx)[0]: r for idx, r in rank.items() if self.int2token(idx)[1] == 'R'}

        if num_rset > 0:
            rset = {token: r for token, r in sorted(rset.items(), key=lambda x: -x[1])[:num_rset]}

        keywords = self._select_keywords(lset, rset)
        keywords = self._filter_compounds(keywords)
        keywords = self._filter_subtokens(keywords)

        if num_keywords > 0:
            keywords = {token: r for token, r in sorted(keywords.items(), key=lambda x: -x[1])[:num_keywords]}

        return keywords, rank, graph

    def _select_keywords(self, lset, rset):
        keywords = {}
        for word, r in sorted(lset.items(), key=lambda x: x[1], reverse=True):
            len_word = len(word)
            if len_word == 1:
                continue

            is_compound = False
            for e in range(2, len_word):
                if (word[:e] in keywords) and (word[:e] in rset):
                    is_compound = True
                    break

            if not is_compound:
                keywords[word] = r

        return keywords

    def _filter_compounds(self, keywords):
        keywords_ = {}
        for word, r in sorted(keywords.items(), key=lambda x: x[1], reverse=True):
            len_word = len(word)

            if len_word <= 2:
                keywords_[word] = r
                continue

            if len_word == 3:
                if word[:2] in keywords_:
                    continue

            is_compound = False
            for e in range(2, len_word - 1):
                # fixed. comment from Y. cho
                if (word[:e] in keywords) and (word[e:] in keywords):
                    is_compound = True
                    break

            if not is_compound:
                keywords_[word] = r

        return keywords_

    def _filter_subtokens(self, keywords):
        subtokens = set()
        keywords_ = {}

        for word, r in sorted(keywords.items(), key=lambda x: x[1], reverse=True):
            subs = {word[:e] for e in range(2, len(word) + 1)}

            is_subtoken = False
            for sub in subs:
                if sub in subtokens:
                    is_subtoken = True
                    break

            if not is_subtoken:
                keywords_[word] = r
                subtokens.update(subs)

        return keywords_

    def train(self, docs, beta=0.85, max_iter=10, vocabulary=None, bias=None):
        """
        It constructs word graph and trains ranks of each node using HITS algorithm.
        Use this function only when you want to train rank of subwords

        Arguments
        ---------
        docs : list of str
            Sentence list.
        beta : float
            PageRank damping factor. 0 < beta < 1
            Default is 0.85
        max_iter : int
            Maximum number of iterations of HITS algorithm.
            Default is 10
        vocabulary : None or dict
            User specified vocabulary to index mapper
        bias : None or dict
            User specified HITS bias term

        Returns
        -------
        rank : dict
            subword : rank dictionary. {int:float}
        graph : dict of dict
            Adjacent subword graph. {int:{int:float}}
        """
        if (not vocabulary) and (not self.vocabulary):
            self.scan_vocabs(docs)
        elif (not vocabulary):
            self.vocabulary = vocabulary
            self._build_index2vocab()

        graph = self._construct_word_graph(docs)

        rank = hits(graph, beta, max_iter, bias,
                    sum_weight=self.sum_weight,
                    number_of_nodes=len(self.vocabulary),
                    verbose=self.verbose
                    )

        return rank, graph

    def token2int(self, token):
        """
        Arguments
        ---------
        token : tuple
            (subword, 'L') or (subword, 'R')
            For example, ('이것', 'L') or ('은', 'R')

        Returns
        -------
        index : int
            Corresponding index
            If it is unknown, it returns -1
        """
        return self.vocabulary.get(token, -1)

    def int2token(self, index):
        """
        Arguments
        ---------
        index : int
            Token index

        Returns
        -------
        token : tuple
            Corresponding index formed such as (subword, 'L') or (subword, 'R')
            For example, ('이것', 'L') or ('은', 'R').
            If it is unknown, it returns None
        """
        return self.index2vocab[index] if (0 <= index < len(self.index2vocab)) else None

    def _construct_word_graph(self, docs):
        def normalize(graph):
            graph_ = defaultdict(lambda: defaultdict(lambda: 0))
            for from_, to_dict in graph.items():
                sum_ = sum(to_dict.values())
                for to_, w in to_dict.items():
                    graph_[to_][from_] = w / sum_
            graph_ = {t: dict(fd) for t, fd in graph_.items()}
            return graph_

        graph = defaultdict(lambda: defaultdict(lambda: 0))
        for doc in docs:

            tokens = doc.split()

            if not tokens:
                continue

            links = []
            for token in tokens:
                links += self._intra_link(token)

            if len(tokens) > 1:
                tokens = [tokens[-1]] + tokens + [tokens[0]]
                links += self._inter_link(tokens)

            links = self._check_token(links)
            if not links:
                continue

            links = self._encode_token(links)
            for l_node, r_node in links:
                graph[l_node][r_node] += 1
                graph[r_node][l_node] += 1

        # reverse for inbound graph. but it normalized with sum of outbound weight
        graph = normalize(graph)
        return graph

    def _intra_link(self, token):
        links = []
        len_token = len(token)
        for e in range(1, min(len_token, 10)):
            if (len_token - e) > self.max_length:
                continue
            links.append(((token[:e], 'L'), (token[e:], 'R')))
        return links

    def _inter_link(self, tokens):
        def rsub_to_token(t_left, t_curr):
            return [((t_left[-b:], 'R'), (t_curr, 'L')) for b in range(1, min(10, len(t_left)))]

        def token_to_lsub(t_curr, t_rigt):
            return [((t_curr, 'L'), (t_rigt[:e], 'L')) for e in range(1, min(10, len(t_rigt)))]

        links = []
        for i in range(1, len(tokens) - 1):
            links += rsub_to_token(tokens[i - 1], tokens[i])
            links += token_to_lsub(tokens[i], tokens[i + 1])
        return links

    def _check_token(self, token_list):
        return [(token[0], token[1]) for token in token_list if
                (token[0] in self.vocabulary and token[1] in self.vocabulary)]

    def _encode_token(self, token_list):
        return [(self.vocabulary[token[0]], self.vocabulary[token[1]]) for token in token_list]

class KeywordVectorizer:
    """
    Arguments
    ---------
    tokenize : callable
        Input format is str, output format is list of str (list of terms)
    vocab_score : dict
        {str:float} form keyword vector

    Attributes
    ----------
    tokenize : callable
        Tokenizer function
    idx_to_vocab : list of str
        Vocab list
    vocab_to_idx : dict
        {str:int} Vocab to index mapper
    keyword_vector : numpy.ndarray
        shape (len(idx_to_vocab),) vector
    """

    def __init__(self, tokenize, vocab_score):
        self.tokenize = tokenize
        self.idx_to_vocab = [vocab for vocab in sorted(vocab_score, key=lambda x:-vocab_score[x])]
        self.vocab_to_idx = {vocab:idx for idx, vocab in enumerate(self.idx_to_vocab)}
        self.keyword_vector = np.asarray(
            [score for _, score in sorted(vocab_score.items(), key=lambda x:-x[1])])
        self.keyword_vector = self._L2_normalize(self.keyword_vector)

    def _L2_normalize(self, vectors):
        return vectors / np.sqrt((vectors ** 2).sum())

    def vectorize(self, sents):
        """
        Argument
        --------
        sents : list of str
            Each str is sentence

        Returns
        -------
        scipy.sparse.csr_matrix
            (n sents, n keywords) shape Boolean matrix
        """
        rows, cols, data = [], [], []
        for i, sent in enumerate(sents):
            terms = set(self.tokenize(sent))
            for term in terms:
                j = self.vocab_to_idx.get(term, -1)
                if j == -1:
                    continue
                rows.append(i)
                cols.append(j)
                data.append(1)
        n_docs = len(sents)
        n_terms = len(self.idx_to_vocab)
        return csr_matrix((data, (rows, cols)), shape=(n_docs, n_terms))


def summarize_with_sentences(texts, num_keywords=100, num_keysents=10, diversity=0.3, stopwords=None, scaling=None,
                             penalty=None, min_count=5, max_length=10, beta=0.85, max_iter=10, num_rset=-1, verbose=False):
    """
    It train KR-WordRank to extract keywords and selects key-sentences to summzriaze inserted texts.

        >>> from krwordrank.sentence import summarize_with_sentences

        >>> texts = [] # list of str
        >>> keywords, sents = summarize_with_sentences(texts, num_keywords=100, num_keysents=10)

    Arguments
    ---------
    texts : list of str
        Each str is a sentence.
    num_keywords : int
        Number of keywords extracted from KR-WordRank
        Default is 100.
    num_keysents : int
        Number of key-sentences selected from keyword vector maching
        Default is 10.
    diversity : float
        Minimum cosine distance between top ranked sentence and others.
        Large value makes this function select various sentence.
        The value must be [0, 1]
    stopwords : None or set of str
        Stopwords list for keyword and key-sentence extraction
    scaling : callable
        Ranking transform function.
        scaling(float) = float
        Default is lambda x:np.sqrt(x)
    penalty : callable
        Penalty function. str -> float
        Default is no penalty
        If you use only sentence whose length is in [25, 40],
        set penalty like following example.

            >>> penalty = lambda x: 0 if 25 <= len(x) <= 40 else 1

    min_count : int
        Minimum frequency of subwords used to construct subword graph
        Default is 5
    max_length : int
        Maximum length of subwords used to construct subword graph
        Default is 10
    beta : float
        PageRank damping factor. 0 < beta < 1
        Default is 0.85
    max_iter : int
        Maximum number of iterations of HITS algorithm.
        Default is 10
    num_rset : int
        Number of R set words sorted by rank. It will be used to L-part word filtering.
        Default is -1.
    verbose : Boolean
        If True, it shows training status
        Default is False

    Returns
    -------
    keysentences : list of str

    Usage
    -----
        >>> from krwordrank.sentence import summarize_with_sentences

        >>> texts = [] # list of str
        >>> keywords, sents = summarize_with_sentences(texts, num_keywords=100, num_keysents=10)
    """

    # train KR-WordRank
    wordrank_extractor = KRWordRank(
        min_count = min_count,
        max_length = max_length,
        verbose = verbose
    )

    num_keywords_ = num_keywords
    if stopwords is not None:
        num_keywords_ += len(stopwords)

    keywords, rank, graph = wordrank_extractor.extract(texts,
                                                       beta, max_iter, num_keywords=num_keywords_, num_rset=num_rset)

    # build tokenizer
    if scaling is None:
        scaling = lambda x:np.sqrt(x)
    if stopwords is None:
        stopwords = {}
    vocab_score = make_vocab_score(keywords, stopwords, scaling=scaling, topk=num_keywords)
    tokenizer = MaxScoreTokenizer(scores=vocab_score)

    # find key-sentences
    sents = keysentence(vocab_score, texts, tokenizer.tokenize, num_keysents, diversity, penalty)
    keywords_ = {vocab:keywords[vocab] for vocab in vocab_score}
    return keywords_, sents

def keysentence(vocab_score, texts, tokenize, topk=10, diversity=0.3, penalty=None):
    """
    Arguments
    ---------
    keywords : {str:int}
        {word:rank} trained from KR-WordRank.
        texts will be tokenized using keywords
    texts : list of str
        Each str is a sentence.
    tokenize : callble
        Tokenize function. Input form is str and output form is list of str
    topk : int
        Number of key sentences
    diversity : float
        Minimum cosine distance between top ranked sentence and others.
        Large value makes this function select various sentence.
        The value must be [0, 1]
    penalty : callable
        Penalty function. str -> float
        Default is no penalty
        If you use only sentence whose length is in [25, 40],
        set penalty like following example.

            >>> penalty = lambda x: 0 if 25 <= len(x) <= 40 else 1

    Returns
    -------
    keysentences : list of str
    """
    if not callable(penalty):
        penalty = lambda x: 0

    if not 0 <= diversity <= 1:
        raise ValueError('Diversity must be [0, 1] float value')

    vectorizer = KeywordVectorizer(tokenize, vocab_score)
    x = vectorizer.vectorize(texts)
    keyvec = vectorizer.keyword_vector.reshape(1,-1)
    initial_penalty = np.asarray([penalty(sent) for sent in texts])
    idxs = select(x, keyvec, texts, initial_penalty, topk, diversity)
    return [texts[idx] for idx in idxs]

def select(x, keyvec, texts, initial_penalty, topk=10, diversity=0.3):
    """
    Arguments
    ---------
    x : scipy.sparse.csr_matrix
        (n docs, n keywords) Boolean matrix
    keyvec : numpy.ndarray
        (1, n keywords) rank vector
    texts : list of str
        Each str is a sentence
    initial_penalty : numpy.ndarray
        (n docs,) shape. Defined from penalty function
    topk : int
        Number of key sentences
    diversity : float
        Minimum cosine distance between top ranked sentence and others.
        Large value makes this function select various sentence.
        The value must be [0, 1]

    Returns
    -------
    keysentence indices : list of int
        The length of keysentences is topk at most.
    """

    dist = pairwise_distances(x, keyvec, metric='cosine').reshape(-1)
    dist = dist + initial_penalty

    idxs = []
    for _ in range(topk):
        idx = dist.argmin()
        idxs.append(idx)
        dist[idx] += 2 # maximum distance of cosine is 2
        idx_all_distance = pairwise_distances(
            x, x[idx].reshape(1,-1), metric='cosine').reshape(-1)
        penalty = np.zeros(idx_all_distance.shape[0])
        penalty[np.where(idx_all_distance < diversity)[0]] = 2
        dist += penalty
    return idxs

def make_vocab_score(keywords, stopwords, negatives=None, scaling=lambda x:x, topk=100):
    """
    Arguments
    ---------
    keywords : dict
        {str:float} word to rank mapper that trained from KR-WordRank
    stopwords : set or dict of str
        Stopword set
    negatives : dict or None
        Penalty term set
    scaling : callable
        number to number. It re-scale rank value of keywords.
    topk : int
        Number of keywords

    Returns
    -------
    keywords_ : dict
        Refined word to score mapper
    """
    if negatives is None:
        negatives = {}
    keywords_ = {}
    for word, rank in sorted(keywords.items(), key=lambda x:-x[1]):
        if len(keywords_) >= topk:
            break
        if word in stopwords:
            continue
        if word in negatives:
            keywords_[word] = negative[word]
        else:
            keywords_[word] = scaling(rank)
    return keywords_
class MaxScoreTokenizer:
    """
    Transplanted from soynlp.tokenizer.MaxScoreTokenizer

    >>> word_score = {'term':0.8, ...}
    >>> tokenizer = MaxScoreTokenizer(word_score)
    >>> tokenizer.tokenize('Example sentence')
    """

    def __init__(self, scores=None, max_length=10, default_score=0.0):
        self._scores = scores if scores else {}
        self._max_length = max_length
        self._ds = default_score

    def __call__(self, sentence, flatten=True):
        return self.tokenize(sentence, flatten)

    def tokenize(self, sentence, flatten=True):
        tokens = [self._recursive_tokenize(token) for token in sentence.split()]
        if flatten:
            tokens = [subtoken[0] for token in tokens for subtoken in token]
        return tokens

    def _recursive_tokenize(self, token, range_l=0, debug=False):

        length = len(token)
        if length <= 2:
            return [(token, 0, length, self._ds, length)]

        if range_l == 0:
            range_l = min(self._max_length, length)

        scores = self._initialize(token, range_l, length)
        if debug:
            pprint(scores)

        result = self._find(scores)

        adds = self._add_inter_subtokens(token, result)

        if result[-1][2] != length:
            adds += self._add_last_subtoken(token, result)

        if result[0][1] != 0:
            adds += self._add_first_subtoken(token, result)

        return sorted(result + adds, key=lambda x:x[1])

    def _initialize(self, token, range_l, length):
        scores = []
        for b in range(0, length - 1):
            for r in range(2, range_l + 1):
                e = b + r

                if e > length:
                    continue

                subtoken = token[b:e]
                score = self._scores.get(subtoken, self._ds)
                scores.append((subtoken, b, e, score, r))

        return sorted(scores, key=lambda x:(-x[3], -x[4], x[1]))

    def _find(self, scores):
        result = []
        num_iter = 0

        while scores:
            word, b, e, score, r = scores.pop(0)
            result.append((word, b, e, score, r))

            if not scores:
                break

            removals = []
            for i, (_1, b_, e_, _2, _3) in enumerate(scores):
                if (b_ < e and b < e_) or (b_ < e and e_ > b):
                    removals.append(i)

            for i in reversed(removals):
                del scores[i]

            num_iter += 1
            if num_iter > 100: break

        return sorted(result, key=lambda x:x[1])

    def _add_inter_subtokens(self, token, result):
        adds = []
        for i, base in enumerate(result[:-1]):
            if base[2] == result[i+1][1]:
                continue

            b = base[2]
            e = result[i+1][1]
            subtoken = token[b:e]
            adds.append((subtoken, b, e, self._ds, e - b))

        return adds

    def _add_first_subtoken(self, token, result):
        e = result[0][1]
        subtoken = token[0:e]
        score = self._scores.get(subtoken, self._ds)
        return [(subtoken, 0, e, score, e)]

    def _add_last_subtoken(self, token, result):
        b = result[-1][2]
        subtoken = token[b:]
        score = self._scores.get(subtoken, self._ds)
        return [(subtoken, b, len(token), score, len(subtoken))]


def highlight_keyword(sent, keywords):
    for keyword, score in keywords.items():
        if score > 0:
            sent = sent.replace(keyword, '[%s]' % keyword)
    return sent

def hits(graph, beta, max_iter=50, bias=None, verbose=True,
         sum_weight=100, number_of_nodes=None, converge=0.001):
    """
    It trains rank of node using HITS algorithm.

    Arguments
    ---------
    graph : dict of dict
        Adjacent subword graph. graph[int][int] = float
    beta : float
        PageRank damping factor
    max_iter : int
        Maximum number of iterations
    bias : None or dict
        Bias vector
    verbose : Boolean
        If True, it shows training progress.
    sum_weight : float
        Sum of weights of all nodes in graph
    number_of_nodes : None or int
        Number of nodes in graph
    converge : float
        Minimum rank difference between previous step and current step.
        If the difference is smaller than converge, it do early-stop.

    Returns
    -------
    rank : dict
        Rank dictionary formed as {int:float}.
    """

    if not bias:
        bias = {}
    if not number_of_nodes:
        number_of_nodes = max(len(graph), len(bias))

    if number_of_nodes <= 1:
        raise ValueError(
            'The graph should consist of at least two nodes\n',
            'The node size of inserted graph is %d' % number_of_nodes
        )

    dw = sum_weight / number_of_nodes
    rank = {node:dw for node in graph.keys()}

    for num_iter in range(1, max_iter + 1):
        rank_ = _update(rank, graph, bias, dw, beta)
        diff = sum((abs(w - rank.get(n, 0)) for n, w in rank_.items()))
        rank = rank_

        if diff < sum_weight * converge:
            if verbose:
                print('\riter = %d Early stopped.' % num_iter, end='', flush=True)
            break

        if verbose:
            print('\riter = %d' % num_iter, end='', flush=True)

    if verbose:
        print('\rdone')

    return rank

def _update(rank, graph, bias, dw, beta):
    rank_new = {}
    for to_node, from_dict in graph.items():
        rank_new[to_node] = sum([w * rank[from_node] for from_node, w in from_dict.items()])
        rank_new[to_node] = beta * rank_new[to_node] + (1 - beta) * bias.get(to_node, dw)
    return rank_new


# Press the green button in the gutter to run the script.
def test(word):
    text=text=word
    texts =  text.replace('?','.').replace('!','.').split('.')
    try:
        keywords, sents = summarize_with_sentences(texts, num_keywords=3, num_keysents=10, diversity=0.3, stopwords=None, scaling=None,
                                                   penalty=None, min_count=3, max_length=10, beta=0.85, max_iter=10, num_rset=-1, verbose=False)
    except NameError:
        return 'null'
    except ValueError:
        return 'null'
    keys=[key for key in keywords]
    return keys