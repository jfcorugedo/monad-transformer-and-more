package com.logicaalternativa.monadtransformerandmore.business.impl;

import static com.logicaalternativa.monadtransformerandmore.monad.MonadContainerWrapper.wrap;

import com.logicaalternativa.monadtransformerandmore.bean.Chapter;
import com.logicaalternativa.monadtransformerandmore.bean.Sales;
import com.logicaalternativa.monadtransformerandmore.bean.Summary;
import com.logicaalternativa.monadtransformerandmore.business.SrvSummaryContainer;
import com.logicaalternativa.monadtransformerandmore.container.Container;
import com.logicaalternativa.monadtransformerandmore.errors.impl.MyError;
import com.logicaalternativa.monadtransformerandmore.monad.MonadContainer;
import com.logicaalternativa.monadtransformerandmore.service.container.ServiceAuthorContainer;
import com.logicaalternativa.monadtransformerandmore.service.container.ServiceBookContainer;
import com.logicaalternativa.monadtransformerandmore.service.container.ServiceChapterContainer;
import com.logicaalternativa.monadtransformerandmore.service.container.ServiceSalesContainer;
import com.logicaalternativa.monadtransformerandmore.errors.Error;
import scala.concurrent.Future;
import scala.util.Either;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SrvSummaryContainerImpl implements SrvSummaryContainer<Error> {
	
	private final ServiceBookContainer<Error> srvBook;
	private final ServiceSalesContainer<Error> srvSales;
	private final ServiceChapterContainer<Error> srvChapter;
	private final ServiceAuthorContainer<Error> srvAuthor;
	
	private final MonadContainer<Error> m;
	

	public SrvSummaryContainerImpl(ServiceBookContainer<Error> srvBook,
			ServiceSalesContainer<Error> srvSales,
			ServiceChapterContainer<Error> srvChapter,
			ServiceAuthorContainer<Error> srvAuthor, MonadContainer<Error> m) {
		super();
		this.srvBook = srvBook;
		this.srvSales = srvSales;
		this.srvChapter = srvChapter;
		this.srvAuthor = srvAuthor;
		this.m = m;
	}



	@Override
	public Container<Error, Summary> getSummary(Integer idBook) {

		Container<Error, Optional<Sales>> salesF = wrap(this.srvSales.getSales(idBook), m)
				.map(sales -> Optional.of(sales))
				.recover(error -> Optional.empty()).value();


		return wrap(m.flatMap2(salesF, this.srvBook.getBook(idBook),
				(sales, book) ->
						m.map2(
								this.srvAuthor.getAuthor(book.getIdAuthor()),
								m.sequence(getChapters(book.getChapters())),
								(author, chapters) -> new Summary(book, chapters, sales, author))
		), m)
				.recoverWith( e -> m.raiseError(new MyError("It is impossible to get book summary")))
				.value();
		
	}

	private List<Container<Error, Chapter>> getChapters(List<Long> chapters) {
		return chapters.stream().map(chId -> this.srvChapter.getChapter(chId)).collect(Collectors.toList());
	}

}
