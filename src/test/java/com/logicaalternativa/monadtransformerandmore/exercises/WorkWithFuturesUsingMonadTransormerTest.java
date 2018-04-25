package com.logicaalternativa.monadtransformerandmore.exercises;

import akka.dispatch.ExecutionContexts;
import akka.dispatch.Futures;
import akka.util.Timeout;
import com.logicaalternativa.monadtransformerandmore.errors.impl.MyError;
import com.logicaalternativa.monadtransformerandmore.model.Person;
import com.logicaalternativa.monadtransformerandmore.monad.MonadFutEither;
import com.logicaalternativa.monadtransformerandmore.monad.impl.MonadFutEitherError;
import org.junit.Test;
import scala.concurrent.Await;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.net.ConnectException;
import java.util.concurrent.Executors;

import static com.logicaalternativa.monadtransformerandmore.monad.MonadFutEitherWrapper.wrap;
import static org.assertj.core.api.Assertions.assertThat;
import com.logicaalternativa.monadtransformerandmore.errors.Error;
import scala.util.Either;
import scala.util.Left;
import scala.util.Right;


public class WorkWithFuturesUsingMonadTransormerTest {

    private static final ExecutionContext EXECUTOR = ExecutionContexts.fromExecutor(Executors.newSingleThreadExecutor());
    private static final Timeout TIMEOUT = new Timeout(Duration.create(5, "seconds"));

    private static final MonadFutEither<Error> monadTransformer = new MonadFutEitherError(EXECUTOR);

    @Test
    public void mapWithMonadTranformer() throws Exception {
        //given
        Future<Either<Error, Person>> personFuture = Futures.successful(new Right<Error, Person>(new Person().setName("Juan").setAge(35)));

        //when
        // REMEMBER: Future<String> nameF = personFuture.map( person -> person.right().get().getName() , EXECUTOR);

        Future<Either<Error,String>> nameF = null;

        //then
        Either<Error,String> name = (Either<Error,String>) Await.result(nameF, TIMEOUT.duration());
        assertThat(name.right().get()).isEqualTo("Juan");
    }

    @Test
    public void mapFailureWithMonadTranformer() throws Exception {
        //given
        Future<Either<Error, Person>> personFuture = Futures.failed(new ConnectException("Ahhhhhhhh!!!!"));

        //when
        Future<Either<Error, String>> nameF = null;

        //then
        Either<Error, String> name = (Either<Error, String>) Await.result(nameF, TIMEOUT.duration());
        assertThat(name.right().get()).isEqualTo("DEFAULT_VALUE");
    }

    @Test
    public void mapErrorWithMonadTranformer() throws Exception {
        //given
        Future<Either<Error, Person>> personFuture = Futures.successful(new Left<>(new MyError("User not found!")));

        //when
        Future<Either<Error, String>> nameF = null;

        //then
        Either<Error, String> name = (Either<Error, String>) Await.result(nameF, TIMEOUT.duration());
        assertThat(name.right().get()).isEqualTo("DEFAULT_VALUE");
    }

    @Test
    public void combineSeveralFuturesWithMonadTransformer() throws Exception {
        //given
        Future<Either<Error, Person>> meFuture = Futures.successful(new Right<>(new Person().setName("Juan").setAge(35)));
        Future<Either<Error, Person>> friendFuture = Futures.successful(new Right<>(new Person().setName("Miguel").setAge(28)));

        //when
        /* REMEMBER:
        Future<Integer> sumAgeF = meFuture.flatMap(
                me -> friendFuture.flatMap(
                        friend -> Futures.successful(me.getAge() + friend.getAge())

                        ,EXECUTOR)
                ,EXECUTOR);
         */

        Future<Either<Error, Integer>> sumAgeF = null;


        //then
        Either<Error, Integer> sumAge = (Either<Error, Integer>) Await.result(sumAgeF, TIMEOUT.duration());
        assertThat(sumAge.right().get()).isEqualTo(63);
    }

    @Test
    public void combineFailedFutureWithAnotherFutureWithMonadTransformer() throws Exception {
        //given
        Future<Either<Error, Person>> meFuture = Futures.failed(new ConnectException("Ahhhhhhhhhh!!!!!"));
        Future<Either<Error, Person>> friendFuture = Futures.successful(new Right<>(new Person().setName("Miguel").setAge(28)));

        //when
        /* REMEMBER:
        Future<Integer> sumAgeF = meFuture.flatMap(
                me -> friendFuture.flatMap(
                        friend -> Futures.successful(me.getAge() + friend.getAge())

                        ,EXECUTOR)
                ,EXECUTOR);
         */

        Future<Either<Error, Integer>> sumAgeF = null;


        //then
        Either<Error, Integer> sumAge = (Either<Error, Integer>) Await.result(sumAgeF, TIMEOUT.duration());
        assertThat(sumAge.right().get()).isEqualTo(-1);
    }

    @Test
    public void combineSeveralDependentFuturesWithMonadTransformer() throws Exception {
        //given
        Future<Either<Error,Person>> meFuture = Futures.successful(new Right<>(new Person().setName("Juan").setAge(35)));

        //NOTE: Use service Future<Person> getFriend(String name)

        //when
        /* REMEMBER:
        Future<Integer> sumAgeF = meFuture
            .flatMap(
                person -> getFriend(person.getName())
                    .map(
                        friend -> friend.getAge()+person.getAge()
                        ,EXECUTOR
                    )
            , EXECUTOR);
        */

        Future<Either<Error, Integer>> sumAgeF = null;


        //then
        Either<Error, Integer> sumAge = (Either<Error, Integer>) Await.result(sumAgeF, TIMEOUT.duration());
        assertThat(sumAge.right().get()).isEqualTo(63);
    }


    @Test
    public void combineFailedFutureWithMonadTransformer() throws Exception {
        //given
        Future<Either<Error,Person>> meFuture = Futures.successful(new Right<>(new Person().setName("Juan").setAge(35)));

        //NOTE: Use service Future<Person> getFailedFriend(String name)

        //when
        /* REMEMBER:
        Future<Integer> sumAgeF = meFuture
            .flatMap(
                person -> getFriend(person.getName())
                    .map(
                        friend -> friend.getAge()+person.getAge()
                        ,EXECUTOR
                    )
            , EXECUTOR);
        */

        Future<Either<Error, Integer>> sumAgeF = null;


        //then
        Either<Error, Integer> sumAge = (Either<Error, Integer>) Await.result(sumAgeF, TIMEOUT.duration());
        assertThat(sumAge.right().get()).isEqualTo(-1);
    }

    @Test
    public void combineThreeFutureWithMonadTransformer() throws Exception {
        //given
        Future<Either<Error,Person>> meFuture = Futures.successful(new Right<>(new Person().setName("Juan").setAge(35)));
        Future<Either<Error,Person>> friend1Future = Futures.successful(new Right<>(new Person().setName("Miguel").setAge(28)));
        Future<Either<Error,Person>> friend2Future = Futures.successful(new Right<>(new Person().setName("Jose Luis").setAge(49)));

        //when
        Future<Either<Error, Integer>> sumAgeF = null;


        //then
        Either<Error, Integer> sumAge = (Either<Error, Integer>) Await.result(sumAgeF, TIMEOUT.duration());
        assertThat(sumAge.right().get()).isEqualTo(112);
    }


    private Future<Either<Error,Person>> getFriend(String name) {

        return Futures.successful(new Right<>(new Person().setName("Miguel").setAge(28)));
    }

    private Future<Either<Error,Person>> getFailedFriend(String name) {

        return Futures.failed(new ConnectException("Connection error!!!!!"));
    }
}
