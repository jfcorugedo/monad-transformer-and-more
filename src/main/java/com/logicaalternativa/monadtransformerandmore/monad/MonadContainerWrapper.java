package com.logicaalternativa.monadtransformerandmore.monad;

import com.logicaalternativa.monadtransformerandmore.container.Container;
import com.logicaalternativa.monadtransformerandmore.function.Function3;
import scala.concurrent.Future;
import scala.util.Either;

import java.util.function.BiFunction;
import java.util.function.Function;

public class MonadContainerWrapper<E, T> {

    private final MonadContainer<E> m;

    private final Container<E, T> fut;

    private MonadContainerWrapper(Container<E, T> fut, MonadContainer<E> m) {
        this.m = m;
        this.fut = fut;
    }

    public static <E,T> MonadContainerWrapper<E, T> wrap(Container<E, T> fut, MonadContainer<E> m) {

        return new MonadContainerWrapper<>(fut, m);
    }

    public Container<E, T> value() {
        return fut;
    }

    public <S> MonadContainerWrapper<E, S> flatMap(Function<T, Container<E,S>> f ){

        return wrap( m.flatMap(fut, f), m);
    }

    public <S> MonadContainerWrapper<E, S> map(Function<T, S> f ){

        return wrap( m.map(fut, f), m);
    }

    public MonadContainerWrapper<E,T> recoverWith(Function<E, Container<E, T>> f ) {

        return wrap( m.recoverWith(fut, f), m);
    }

    public MonadContainerWrapper<E,T> recover(Function<E, T> f ) {

        return wrap( m.recover(fut, f), m );
    }

    public <B, S> MonadContainerWrapper<E,S> map2(Container<E, B> fromB, BiFunction<T,B,S> f  ) {

        return wrap( m.map2(fut, fromB, f), m );
    }

    public <B,C,S> MonadContainerWrapper<E,S> map3(Container<E, B> fromB,
                                                   Container<E, C> fromC,
                                                   Function3<T,B,C,S> f  ) {

        return wrap( m.map3(fut, fromB, fromC, f), m );
    }
}
