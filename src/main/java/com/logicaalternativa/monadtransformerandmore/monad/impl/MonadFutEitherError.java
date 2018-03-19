package com.logicaalternativa.monadtransformerandmore.monad.impl;

import static com.logicaalternativa.monadtransformerandmore.util.TDD.$_notYetImpl;

import java.util.function.Function;

import akka.dispatch.Futures;
import com.logicaalternativa.monadtransformerandmore.errors.impl.MyError;
import com.logicaalternativa.monadtransformerandmore.util.Java8;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.util.Either;
import com.logicaalternativa.monadtransformerandmore.errors.Error;

import com.logicaalternativa.monadtransformerandmore.monad.MonadFutEither;
import scala.util.Left;
import scala.util.Right;

public class MonadFutEitherError implements MonadFutEither<Error> {
	
	final ExecutionContext ec;
	
	
	public MonadFutEitherError(ExecutionContext ec) {
		super();
		this.ec = ec;
	}

	@Override
	public <T> Future<Either<Error, T>> pure(T value) {
		
		return Futures.successful(new Right<>(value));
	}

	@Override
	public <A, T> Future<Either<Error, T>> flatMap(
			Future<Either<Error, A>> from,
			Function<A, Future<Either<Error, T>>> f) {
		
		return from
				.flatMap( a -> a.isRight() ? f.apply(a.right().get()) : raiseError(a.left().get()), ec)
				.recoverWith(Java8.recoverF(t -> raiseError(new MyError(t.getMessage()))), ec);
	}

	@Override
	public <T> Future<Either<Error, T>> raiseError(Error error) {
		
		return Futures.successful(new Left<>(error));
	}

	@Override
	public <T> Future<Either<Error, T>> recoverWith(
			Future<Either<Error, T>> from,
			Function<Error, Future<Either<Error, T>>> f) {
		
		return from
				.flatMap(t -> t.isRight() ? pure(t.right().get()) : f.apply(t.left().get()) , ec)
				.recoverWith(Java8.recoverF( t-> raiseError(new MyError(t.getMessage()))), ec);
	}
}
