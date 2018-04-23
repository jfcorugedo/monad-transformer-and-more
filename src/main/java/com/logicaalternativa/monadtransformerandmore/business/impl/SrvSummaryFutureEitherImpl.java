package com.logicaalternativa.monadtransformerandmore.business.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.logicaalternativa.monadtransformerandmore.monad.MonadFutEitherWrapper.*;

import scala.concurrent.Future;
import scala.util.Either;
import com.logicaalternativa.monadtransformerandmore.bean.Chapter;
import com.logicaalternativa.monadtransformerandmore.bean.Sales;
import com.logicaalternativa.monadtransformerandmore.bean.Summary;
import com.logicaalternativa.monadtransformerandmore.business.SrvSummaryFutureEither;
import com.logicaalternativa.monadtransformerandmore.errors.Error;
import com.logicaalternativa.monadtransformerandmore.errors.impl.MyError;
import com.logicaalternativa.monadtransformerandmore.monad.MonadFutEither;
import com.logicaalternativa.monadtransformerandmore.service.future.ServiceAuthorFutEither;
import com.logicaalternativa.monadtransformerandmore.service.future.ServiceBookFutEither;
import com.logicaalternativa.monadtransformerandmore.service.future.ServiceChapterFutEither;
import com.logicaalternativa.monadtransformerandmore.service.future.ServiceSalesFutEither;


public class SrvSummaryFutureEitherImpl implements SrvSummaryFutureEither<Error> {

	private final ServiceBookFutEither<Error> srvBook;
	private final ServiceSalesFutEither<Error> srvSales;
	private final ServiceChapterFutEither<Error> srvChapter;
	private final ServiceAuthorFutEither<Error> srvAuthor;
	
	private final MonadFutEither<Error> m;
	
	
	public SrvSummaryFutureEitherImpl(ServiceBookFutEither<Error> srvBook,
			ServiceSalesFutEither<Error> srvSales,
			ServiceChapterFutEither<Error> srvChapter,
			ServiceAuthorFutEither<Error> srvAuthor,
			MonadFutEither<Error> m) {
		super();
		this.srvBook = srvBook;
		this.srvSales = srvSales;
		this.srvChapter = srvChapter;
		this.srvAuthor = srvAuthor;
		this.m = m;
	}

	@Override
	public Future<Either<Error, Summary>> getSummary(Integer idBook) {



































		Future<Either<Error, Optional<Sales>>> salesF = wrap(this.srvSales.getSales(idBook), m)
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



		/*
		Another way:

		return m.flatMap2(salesF, this.srvBook.getBook(idBook),
					(sales, book) -> {
						return m.map2(this.srvAuthor.getAuthor(book.getIdAuthor()), m.sequence(getChapters(book.getChapters())), (author, chapters) -> {
							return new Summary(book, chapters, sales, author);
						});
					}
		);

		 */
		/*
		Another way:
		return wrap(this.srvBook.getBook(idBook), m)
				.flatMap(book -> wrap(
						m.sequence(
								book.getChapters().stream().map(chId -> this.srvChapter.getChapter(chId)).collect(Collectors.toList())
						)
						, m).flatMap(chapters ->  wrap( this.srvAuthor.getAuthor(book.getIdAuthor()), m).map(
										author -> new Summary(book, chapters, sales, author)
							).value()
						).value()
				).value();
		 */


	}

	private List<Future<Either<Error, Chapter>>> getChapters(List<Long> chapters) {
		return chapters.stream().map(chId -> this.srvChapter.getChapter(chId)).collect(Collectors.toList());
	}
}
