package com.logicaalternativa.monadtransformerandmore.business.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.logicaalternativa.monadtransformerandmore.monad.MonadFutEitherWrapper.*;

import com.logicaalternativa.monadtransformerandmore.bean.*;
import scala.concurrent.Future;
import scala.util.Either;
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


		//TODO: Code the logic to build a Future<Either<Error, Summary>>

		Future<Either<Error, Book>> book = srvBook.getBook(idBook);
		Future<Either<Error, Optional<Sales>>> sales = wrap(srvSales.getSales(idBook), m)
				.map(s -> Optional.of(s))
				.recover(error -> Optional.empty()).value();
		Future<Either<Error, Author>> author = wrap(book, m).flatMap(b -> srvAuthor.getAuthor(b.getIdAuthor())).value();
		Future<Either<Error, List<Chapter>>> chapters = wrap(book, m).flatMap(b -> m.sequence(getChapters(b.getChapters()))).value();

		Future<Either<Error, Summary>> summary = wrap(book, m)
				.flatMap(
						b -> m.map3(sales, author, chapters,
						(s, a, c) -> new Summary(b, c, s, a)))
				.recoverWith(error -> m.raiseError(new MyError("It is impossible to get book summary"))).value();

		// Other ways of doing the same stuff
		//
		//
		// return wrap(book, m).flatMap(
		//		b -> m.flatMap(sales,
		//		s -> m.flatMap(author,
		//		a -> m.map(chapters,
		//		c -> new Summary(b, c, Optional.of(s), a))))).value();
		//
		//
		// return m.flatMap2(salesF, this.srvBook.getBook(idBook),
		//			(sales, book) -> {
		//				return m.map2(this.srvAuthor.getAuthor(book.getIdAuthor()), m.sequence(getChapters(book.getChapters())), (author, chapters) -> {
		//					return new Summary(book, chapters, sales, author);
		//				});
		//			}
		//	);
		//
		//
		// return wrap(this.srvBook.getBook(idBook), m)
		//		.flatMap(book -> wrap(
		//				m.sequence(
		//						book.getChapters().stream().map(chId -> this.srvChapter.getChapter(chId)).collect(Collectors.toList())
		//				)
		//				, m).flatMap(chapters ->  wrap( this.srvAuthor.getAuthor(book.getIdAuthor()), m).map(
		//						author -> new Summary(book, chapters, sales, author)
		//					).value()
		//				).value()
		//		).value();

		return summary;
	}

	private List<Future<Either<Error, Chapter>>> getChapters(List<Long> chapters) {
		return chapters.stream().map(chId -> this.srvChapter.getChapter(chId)).collect(Collectors.toList());
	}
}
