import functions.*;
import functions.basic.*;
import functions.meta.*;
import java.io.*;
import java.util.concurrent.Semaphore;

import threads.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("---------------Тест итераторов---------------");
        TabulatedFunction fun = new ArrayTabulatedFunction( 0, 10, 9);
        for (FunctionPoint p : fun) {
            System.out.println(p);
        }

        System.out.println();
        System.out.println("------------Тест фабричного метода------------");
        Function f = new Cos();
        TabulatedFunction tf;
        tf = TabulatedFunctions.tabulate(f, 0, Math.PI, 11);
        System.out.println(tf.getClass());
        TabulatedFunctions.setTabulatedFunctionFactory(new
                LinkedListTabulatedFunction.LinkedListTabulatedFunctionFactory());
        tf = TabulatedFunctions.tabulate(f, 0, Math.PI, 11);
        System.out.println(tf.getClass());
        TabulatedFunctions.setTabulatedFunctionFactory(new
                ArrayTabulatedFunction.ArrayTabulatedFunctionFactory());
        tf = TabulatedFunctions.tabulate(f, 0, Math.PI, 11);
        System.out.println(tf.getClass());

        System.out.println();
        System.out.println("--------------Тест рефлексии--------------");
        TabulatedFunction function;

        function = TabulatedFunctions.createTabulatedFunction(
                ArrayTabulatedFunction.class, 0, 10, 3);
        System.out.println(function.getClass());
        System.out.println(function);

        function = TabulatedFunctions.createTabulatedFunction(
                ArrayTabulatedFunction.class, 0, 10, new double[] {0, 10});
        System.out.println(function.getClass());
        System.out.println(function);

        function = TabulatedFunctions.createTabulatedFunction(
                LinkedListTabulatedFunction.class,
                new FunctionPoint[] {
                        new FunctionPoint(0, 0),
                        new FunctionPoint(10, 10)
                }
        );
        System.out.println(function.getClass());
        System.out.println(function);

        function = TabulatedFunctions.tabulate(
                LinkedListTabulatedFunction.class, new Sin(), 0, Math.PI, 11);
        System.out.println(function.getClass());
        System.out.println(function);
    }
}