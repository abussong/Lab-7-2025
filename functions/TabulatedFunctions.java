package functions;

import java.io.*;
public class TabulatedFunctions {
    private TabulatedFunctions() {throw new AssertionError("Нельзя создавать экземпляры утилитного класса");}
    private static TabulatedFunctionFactory factory = new ArrayTabulatedFunction.ArrayTabulatedFunctionFactory();
    public static void setTabulatedFunctionFactory(TabulatedFunctionFactory factory) {TabulatedFunctions.factory = factory;}

    //новые методы создания через фабрику
    public static TabulatedFunction createTabulatedFunction(double leftX, double rightX, int pointsCount) {
        return factory.createTabulatedFunction(leftX, rightX, pointsCount);
    }

    public static TabulatedFunction createTabulatedFunction(double leftX, double rightX, double[] values) {
        return factory.createTabulatedFunction(leftX, rightX, values);
    }

    public static TabulatedFunction createTabulatedFunction(FunctionPoint[] points) {
        return factory.createTabulatedFunction(points);
    }

    public static TabulatedFunction tabulate(Function function, double leftX, double rightX, int pointsCount) {
        //проверяем возможные ошибки в параметрах и корректность границ табулирования
        if (leftX>=rightX) throw new IllegalArgumentException("Левая граница должна быть меньше правой!");
        if (pointsCount < 2) throw new IllegalArgumentException("Точек должно быть минимум две!");
        if (leftX < function.getLeftDomainBorder() || rightX > function.getRightDomainBorder()) {throw new IllegalArgumentException("Границы табулирования выходят за область определения функции!");}

        //создаем табулированную функцию, используя TabulatedFunction
        TabulatedFunction result = createTabulatedFunction(leftX, rightX, pointsCount);

        //заполняем значениями исходной функции
        for (int i = 0; i < pointsCount; i++) {
            double x = result.getPointX(i);
            double y = function.getFunctionValue(x);
            result.setPointY(i, y);
        }

        return result;
    }

    //методы рефлексивного создания объектов
    public static TabulatedFunction createTabulatedFunction(Class<?> functionClass, double leftX, double rightX, int pointsCount) {
        try {
            //проверяем, что класс реализует TabulatedFunction
            if (!TabulatedFunction.class.isAssignableFrom(functionClass)) {
                throw new IllegalArgumentException("Класс должен реализовывать интерфейс TabulatedFunction!");
            }

            //ищем конструктор с параметрами (double, double, int)
            java.lang.reflect.Constructor<?> constructor = functionClass.getConstructor(double.class, double.class, int.class);
            //создаём объект
            return (TabulatedFunction) constructor.newInstance(leftX, rightX, pointsCount);

        } catch (Exception e) {throw new IllegalArgumentException("Невозможно создать экземпляр табулированной функции: ", e);}
    }

    public static TabulatedFunction createTabulatedFunction(Class<?> functionClass, double leftX, double rightX, double[] values) {
        try {
            if (!TabulatedFunction.class.isAssignableFrom(functionClass)) {
                throw new IllegalArgumentException("Класс должен реализовывать интерфейс TabulatedFunction!");
            }

            //ищем нужный конструктор и создаём объект
            java.lang.reflect.Constructor<?> constructor = functionClass.getConstructor(double.class, double.class, double[].class);
            return (TabulatedFunction) constructor.newInstance(leftX, rightX, values);

        } catch (Exception e) {throw new IllegalArgumentException("Невозможно создать экземпляр табулированной функции: ", e);}
    }

    public static TabulatedFunction createTabulatedFunction(Class<?> functionClass, FunctionPoint[] points) {
        try {
            if (!TabulatedFunction.class.isAssignableFrom(functionClass)) {
                throw new IllegalArgumentException("Класс должен реализовывать интерфейс TabulatedFunction!");
            }

            //ищем нужный конструктор и создаём объект
            java.lang.reflect.Constructor<?> constructor = functionClass.getConstructor(FunctionPoint[].class);
            return (TabulatedFunction) constructor.newInstance((Object) points);

        } catch (Exception e) {throw new IllegalArgumentException("Невозможно создать экземпляр табулированной функции: ", e);}
    }

    //перегруженный метод табулирования с рефлексией
    public static TabulatedFunction tabulate(Class<?> functionClass, Function function, double leftX, double rightX, int pointsCount) {
        //проверяем возможные ошибки в параметрах и корректность границ табулирования
        if (leftX>=rightX) throw new IllegalArgumentException("Левая граница должна быть меньше правой!");
        if (pointsCount < 2) throw new IllegalArgumentException("Точек должно быть минимум две!");
        if (leftX < function.getLeftDomainBorder() || rightX > function.getRightDomainBorder()) {throw new IllegalArgumentException("Границы табулирования выходят за область определения функции!");}

        //создаем табулированную функцию, используя TabulatedFunction
        //проверки в данном случае выполняются при попытке вызова конструктора, ошибки обрабатыва.тся там же
        TabulatedFunction result = createTabulatedFunction(functionClass, leftX, rightX, pointsCount);

        //заполняем значениями исходной функции
        for (int i = 0; i < pointsCount; i++) {
            double x = result.getPointX(i);
            double y = function.getFunctionValue(x);
            result.setPointY(i, y);
        }
        return result;
    }

    public static TabulatedFunction inputTabulatedFunction(Class<?> functionClass, InputStream in) throws IOException {
        try {
            if (!TabulatedFunction.class.isAssignableFrom(functionClass)) {
                throw new IllegalArgumentException("Класс должен реализовывать интерфейс TabulatedFunction!");
            }
            DataInputStream dis = new DataInputStream(in);
            //читаем количество точек
            int pointsCount = dis.readInt();

            //читаем координаты точек
            FunctionPoint[] points = new FunctionPoint[pointsCount];
            for (int i = 0; i < pointsCount; i++) {
                points[i] = new FunctionPoint(dis.readDouble(), dis.readDouble());
            }

            //создаем табулированную функцию через рефлексию
            java.lang.reflect.Constructor<?> constructor = functionClass.getConstructor(FunctionPoint[].class);
            return (TabulatedFunction) constructor.newInstance((Object) points);
        } catch (Exception e) {throw new IllegalArgumentException("Невозможно создать экземпляр табулированной функции: ", e);}
    }

    public static TabulatedFunction readTabulatedFunction(Class<?> functionClass, Reader in) throws IOException {
        try {
            if (!TabulatedFunction.class.isAssignableFrom(functionClass)) {
                throw new IllegalArgumentException("Класс должен реализовывать интерфейс TabulatedFunction!");
            }
            StreamTokenizer st = new StreamTokenizer(in);

            //настройка токенизатора для чтения чисел
            st.resetSyntax();

            st.wordChars('0', '9');
            st.wordChars('.', '.');
            st.wordChars('-', '-');
            st.wordChars('E', 'E');
            st.wordChars('e', 'e');

            st.whitespaceChars(' ', ' ');
            st.whitespaceChars('(', '(');
            st.whitespaceChars(')', ')');
            st.whitespaceChars(',', ',');
            st.whitespaceChars('\t', '\t');
            st.whitespaceChars('\n', '\n');
            st.whitespaceChars('\r', '\r');

            //читаем количество точек
            if (st.nextToken() != StreamTokenizer.TT_WORD) {
                throw new RuntimeException("Ожидалось количество точек!");
            }
            int pointsCount = Integer.parseInt(st.sval);

            //читаем координаты точек
            FunctionPoint[] points = new FunctionPoint[pointsCount];
            for (int i = 0; i < pointsCount; i++) {
                //читаем X
                if (st.nextToken() != StreamTokenizer.TT_WORD) {
                    throw new RuntimeException("Ожидалась координата X!");
                }
                double x = Double.parseDouble(st.sval);

                //читаем Y
                if (st.nextToken() != StreamTokenizer.TT_WORD) {
                    throw new RuntimeException("Ожидалась координата Y!");
                }
                double y = Double.parseDouble(st.sval);

                points[i] = new FunctionPoint(x, y);
            }

            //создаем объект через рефлексию
            java.lang.reflect.Constructor<?> constructor = functionClass.getConstructor(FunctionPoint[].class);
            return (TabulatedFunction) constructor.newInstance((Object) points);
        } catch (Exception e) {throw new IllegalArgumentException("Невозможно создать экземпляр табулированной функции: ", e);}
    }

    public static void outputTabulatedFunction(TabulatedFunction function, OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
            //записываем количество точек
            dos.writeInt(function.getPointsCount());

            //записываем координаты всех точек
            for (int i = 0; i < function.getPointsCount(); i++) {
                dos.writeDouble(function.getPointX(i));
                dos.writeDouble(function.getPointY(i));
            }
        dos.flush();
    }

    public static TabulatedFunction inputTabulatedFunction(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
            //читаем количество точек
            int pointsCount = dis.readInt();

            //читаем координаты точек
            FunctionPoint[] points = new FunctionPoint[pointsCount];
            for (int i = 0; i < pointsCount; i++) {
                points[i] = new FunctionPoint(dis.readDouble(), dis.readDouble());
            }

            //создаем табулированную функцию (реализация через ArrayTabulatedFunction как в методе табулирования)
            return factory.createTabulatedFunction(points);
    }

    public static void writeTabulatedFunction(TabulatedFunction function, Writer out) throws IOException {
        BufferedWriter bw = new BufferedWriter(out);
        bw.write(Integer.toString(function.getPointsCount()));
        bw.newLine();

        //записываем координаты всех точек через пробел
        for (int i = 0; i < function.getPointsCount(); i++) {
            bw.write("("+function.getPointX(i) + ", " + function.getPointY(i) + ")");
            bw.newLine();
        }
        bw.flush();
    }

    public static TabulatedFunction readTabulatedFunction(Reader in) throws IOException {
        StreamTokenizer st = new StreamTokenizer(in);

            //настройка токенизатора для чтения чисел
            st.resetSyntax();

            st.wordChars('0', '9');
            st.wordChars('.', '.');
            st.wordChars('-', '-');
            st.wordChars('E', 'E');
            st.wordChars('e', 'e');

            st.whitespaceChars(' ', ' ');
            st.whitespaceChars('(','(');
            st.whitespaceChars(')',')');
            st.whitespaceChars(',',',');
            st.whitespaceChars('\t', '\t');
            st.whitespaceChars('\n', '\n');
            st.whitespaceChars('\r', '\r');

            //читаем количество точек
            if (st.nextToken() != StreamTokenizer.TT_WORD) {throw new RuntimeException("Ожидалось количество точек!");}
            int pointsCount = Integer.parseInt(st.sval);

            //читаем координаты точек
            FunctionPoint[] points = new FunctionPoint[pointsCount];
            for (int i = 0; i < pointsCount; i++) {
                //читаем X
                if (st.nextToken() != StreamTokenizer.TT_WORD) {throw new RuntimeException("Ожидалась координата X!");}
                double x = Double.parseDouble(st.sval);

                //читаем Y
                if (st.nextToken() != StreamTokenizer.TT_WORD) {throw new RuntimeException("Ожидалась координата Y!");}
                double y = Double.parseDouble(st.sval);

                points[i] = new FunctionPoint(x, y);
            }

            return factory.createTabulatedFunction(points);
    }
}
