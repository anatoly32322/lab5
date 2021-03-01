package com;

import com.Exceptions.WrongInputException;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Это класс управляющий коллекцией
 */
public class Commands {
    /**
     * Коллекция
     */
    private ArrayDeque<Route> data = new ArrayDeque<>();
    /**
     * Время создания
     */
    private ZonedDateTime date = ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("Asia/Tokyo"));
    /**
     * Путь до файла
     */
    private String path;
    private HashMap<String, String> manual;

    {
        manual = new HashMap<>();
        manual.put("help", "Справка по доступным командам");
        manual.put("info", "Общая информация о коллекции");
        manual.put("show", "Вывод всех данных, хранящихся в коллекции");
        manual.put("add", "Добавление элемента в коллекцию");
        manual.put("update", "Обновление значения элемента, id которого равен заданному");
        manual.put("remove_by_id", "Удаление элемента коллекции по его id");
        manual.put("clear", "Очистка коллекции");
        manual.put("save", "Сохрание коллекции в файл");
        manual.put("execute_script", "Считать и исполнить скрипт из указанного файла");
        manual.put("exit", "Завершение работы программы");
        manual.put("add_if_max", "Добавить новый элемент в коллекцию, если его значение поля distant превышает значение наибольшего элемента коллекции");
        manual.put("add_if_min", "Добавить новый элемент в коллекцию, если его значение поля distant меньше, чем значение наименьшего элемента коллекции");
        manual.put("remove_lower", "Удалить из коллекции все элементы, меньшие, чем данный");
        manual.put("min_by_id", "Вывести элемень коллекции с наименьшим значением поля id");
        manual.put("group_counting_by_distance", "Группировка элементов коллекции по значению поля distance");
        manual.put("count_by_distance", "Вывод количества элементов коллекции, значение поля distance которых равно данному");
    }

    public Commands(String path) {
        this.path = path;
    }

    /**
     * Этот метод возвращает справку обо всех методах данного класса
     */
    private void help() {
        for (Map.Entry<String, String> pair : manual.entrySet()){
            String key = pair.getKey();
            String value = pair.getValue();
            System.out.println(key + " " + value);
        }
    }

    /**
     * Этот метод возвращает информацию о коллекции: название, время создания и т.д.
     * @throws IllegalAccessException
     */
    private void info() throws IllegalAccessException {
        Class cl = data.getClass();
        System.out.println("Название контейнера: " + cl.getName());
        System.out.println("Время создания: " + date);
        System.out.println("Данные хранимые в контейнере:\n" + show());
    }

    /**
     * Этот метод возвращает значение всех данных, хранящихся в коллекции
     * @return
     * @throws IllegalAccessException
     */
    private String show() throws IllegalAccessException {
        String text = "";
        for (Route i : data) {
            text += showRoute(i);
        }
        return text;
    }

    /**
     * Этот метод выводит информацию об определённом объекте класса Route
     * @param a Передаваемый объект класса Route
     * @return Возвращает информацию о данном объекте
     * @throws IllegalAccessException
     */
    private String showRoute(Route a) throws IllegalAccessException {
        String text = a.toString();
        return text;
    }

    /**
     * Этот метод добавляет объект класса Route в коллекцию
     * @param a Передаваемый объект класса Route
     */
    private void add(Route a) {
        data.addLast(a);
        sort();
    }

    /**
     * Этот метод обновляет значение элемента коллекции, id которого равен данному
     * @param id Передаваемый id
     * @param a Передаваемый объект класса Route
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private void update(int id, Route a) throws NoSuchFieldException, IllegalAccessException {
        for (Route i : data) {
            if (i.getId() == id) {
                Class cl = i.getClass();
                Field[] fields = cl.getDeclaredFields();
                for (Field j : fields) {
                    j.setAccessible(true);
                    j.set(i, a.getClass().getDeclaredField(j.getName()).get(a));
                    j.setAccessible(false);
                }
                break;
            }
        }
    }

    /**
     * Этот метод удаляет элемент из коллекции по его id
     * @param id Передаваемый id
     */
    private void remove_by_id(int id) {
        for (Route i : data) {
            if (i.getId() == id) {
                data.removeFirstOccurrence(i);
                break;
            }
        }
    }

    /**
     * Этот метод очищает коллекцию
     */
    private void clear() {
        data.clear();
    }

    /**
     * Этот метод сохраняет все элементы коллекции в файл
     * @throws IOException
     */
    private void save() throws IOException {
        FileOutputStream out = new FileOutputStream(path);
        String text = "";
        for (Route rt : data) {
            System.out.println(rt.toCSV());
            text = rt.toCSV() + '\n';
            byte[] buffer = text.getBytes();
            for (byte i : buffer) {
                out.write(i);
            }
        }
        out.close();
    }

    /**
     * Этот метод считывает и исполняет скрипт из указанного файла
     */
    private void execute_script(String path) {
        String line = "";
        String[] fields = new String[]{"name", "coordinates", "from", "to"};
        Route rt = new Route();
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            while ((line = br.readLine()) != null) {
                line.replaceAll("\n", "");
                line.trim();
                String[] ln = line.split(" ");
                switch (ln[0]){
                    case "help":
                        help();
                        break;
                    case "info":
                        info();
                        break;
                    case "show":
                        System.out.println(show());
                        break;
                    case "add":
                        rt = getRoute(br);
                        add(rt);
                        break;
                    case "update":
                        rt = getRoute(br);
                        update(Integer.parseInt(ln[1]), rt);
                        break;
                    case "remove_by_id":
                        remove_by_id(Integer.parseInt(ln[1]));
                        break;
                    case "clear":
                        clear();
                        break;
                    case "save":
                        save();
                        break;
                    case "exit":
                        exit();
                        break;
                    case "add_if_max":
                        rt = getRoute(br);
                        add_if_max(rt);
                        break;
                    case "add_if_min":
                        rt = getRoute(br);
                        add_if_min(rt);
                        break;
                    case "remove_lower":
                        rt = getRoute(br);
                        remove_lower(rt);
                        break;
                    case "min_by_id":
                        min_by_id();
                        break;
                    case "group_counting_by_distance":
                        group_counting_by_distance();
                        break;
                    case "count_by_distance":
                        count_by_distance(Long.parseLong(ln[1]));
                        break;
                    case "execute_script":
                        execute_script(ln[1]);
                        break;
                    default:
                        throw new WrongInputException("Введена неверная команда. Повторите ввод.");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (WrongInputException e){
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Этот метод завершает программу
     */
    private void exit() {
        System.exit(0);
    }

    /**
     * Этот метод добавляет элемент в коллекцию, если его значение поля distance превышает наибольшее значение элемента коллекции
     * @param a Передаваемый объект класса Route
     */
    private void add_if_max(Route a) {
        Double dist = a.getDistance();
        if (dist > data.getLast().getDistance()) {
            add(a);
        }
    }

    /**
     * Этот метод добавляет элемент в коллекцию, если его значение поля distance меньше, чем наименьшее значение элемента коллекции
     * @param a Передаваемый объект класса Route
     */
    private void add_if_min(Route a) {
        Double dist = a.getDistance();
        if (dist < data.getFirst().getDistance()) {
            add(a);
        }
    }

    /**
     * Этот метод удаляет элементы из коллекции, меньшие, чем заданный
     * @param a Передаваемый объект класса Route
     */
    private void remove_lower(Route a) {
        Double dist = a.getDistance();
        ArrayDeque<Route> deq = new ArrayDeque<Route>();
        for (Route i : data) {
            if (i.getDistance() >= dist) {
                deq.addLast(i);
            }
        }
        data = deq.clone();
    }

    /**
     * Этот метод выводит значение элемента коллекции с нименьшим значением поля id
     * @throws IllegalAccessException
     */
    private void min_by_id() throws IllegalAccessException {
        int min_id = data.getFirst().getId();
        Route min_by_id = data.getFirst();
        for (Route i : data) {
            if (min_id > i.getId()) {
                min_id = i.getId();
                min_by_id = i;
            }
        }
        System.out.println(showRoute(min_by_id));
    }

    /**
     * Этот метод группирует элементы коллекции по значению поля distance
     */
    private void group_counting_by_distance() {
        String text = "";
        int count = 0;
        Double dist = data.getFirst().getDistance();
        for (Route i : data) {
            if (i.getDistance() == dist) {
                count++;
            } else {
                text += "Distance = " + dist + " : " + count + '\n';
                count = 1;
                dist = i.getDistance();
            }
        }
        text += "Distance = " + dist + " : " + count + '\n';
        System.out.println(text);
    }

    /**
     * Этот метод выводит количество элементов, значение поля distance которых равно заданному
     * @param dist Передаваемое значение поля distance
     */
    private void count_by_distance(long dist) {
        int count = 0;
        for (Route i : data) {
            if (i.getDistance() == dist) {
                count++;
            }
        }
        System.out.println("Всего " + count + " объектов с полем Distance равным " + dist + '\n');
    }

    /**
     * Сортировка элементов коллекции. Выполняется по полю distance
     */
    private void sort() {
        Route[] arr = new Route[data.size()];
        Integer cnt = 0;
        for (Route i : data) {
            arr[cnt] = i;
            cnt++;
        }
        Arrays.sort(arr, new Comparator<Route>() {
            @Override
            public int compare(Route o1, Route o2) {
                return (int) (o1.getDistance() - o2.getDistance());
            }
        });
        data.clear();
        for (Route i : arr) {
            data.addLast(i);
        }
    }

    /**
     * Вычисление расстояния между двумя точками пространства
     * @param a Координаты первой точки
     * @param b Координаты второй точки
     * @return Возвращает значение типа Double
     */
    private Double calculateDistance(Coordinates a, Coordinates b) {
        Double dist = Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2) + Math.pow(a.z - b.z, 2));
        return dist;
    }

    /**
     * Считывает данные с входного потока и обрабатывает их, преобразуя в объект типа Route
     * @param br Пермененная считывающая поток входных данных
     * @return Возвращает объект типа Route
     */
    private Route getRoute(BufferedReader br) {
        String line = "";
        String[] fields = new String[]{"name", "coordinates", "from", "to"};
        Route rt = new Route();
        try {
            for (String field : fields) {
                if (field.equals("name")) {
                    System.out.print("Введите значение поля name: ");
                    System.out.print('\n');
                    while (true){
                        try {
                            line = br.readLine();
                            if (line.length() == 0){
                                throw new WrongInputException("Поле name должно быть заполнено. Повторите попытку");
                            }
                            rt.setName(line);
                            break;
                        } catch (WrongInputException e){
                            e.printStackTrace();
                        }
                    }
                } else if (field.equals("coordinates")) {
                    System.out.print("Введите значение поля coordinates по следующему шаблону: x y z, - где x - Integer; y - Double; z - Double: ");
                    System.out.print('\n');
                    while (true){
                        try {
                            line = br.readLine();
                            String[] xyz = line.split(" ");
                            if (xyz.length != 3){
                                throw new WrongInputException("Неверный формат ввода. Повторите попытку");
                            }
                            Long x = Long.parseLong(xyz[0]);
                            Double y = Double.parseDouble(xyz[1]);
                            Double z = Double.parseDouble(xyz[2]);
                            rt.setCoordinates(new Coordinates(x, y, z));
                            break;
                        } catch (WrongInputException e){
                            e.printStackTrace();
                        }
                    }
                } else if (field.equals("from")) {
                    System.out.print("Введите значение поля from. Оно может иметь следующие значения: " + java.util.Arrays.asList(Location.values()));
                    System.out.print('\n');
                    while (true) {
                        try {
                            line = br.readLine();
                            line = line.toLowerCase();
                            switch (line) {
                                case "russia":
                                    rt.setFrom(Location.RUSSIA);
                                    break;
                                case "usa":
                                    rt.setFrom(Location.USA);
                                    break;
                                case "norway":
                                    rt.setFrom(Location.NORWAY);
                                    break;
                                case "italy":
                                    rt.setFrom(Location.ITALY);
                                    break;
                                case "france":
                                    rt.setFrom(Location.FRANCE);
                                    break;
                                default:
                                    if (line.length() != 0){
                                        throw new WrongInputException("Неверно введены данные. Повторите попытку");
                                    }
                                    rt.setFrom(Location.NULL);
                            }
                            break;
                        } catch (WrongInputException e){
                            e.printStackTrace();
                        }
                    }
                } else if (field.equals("to")) {
                    System.out.print("Введите значение поля to. Оно может иметь следующие значения: " + java.util.Arrays.asList(Location.values()));
                    System.out.print('\n');
                    while (true) {
                        try {
                            line = br.readLine();
                            line = line.toLowerCase();
                            switch (line) {
                                case "russia":
                                    rt.setTo(Location.RUSSIA);
                                    break;
                                case "usa":
                                    rt.setTo(Location.USA);
                                    break;
                                case "norway":
                                    rt.setTo(Location.NORWAY);
                                    break;
                                case "italy":
                                    rt.setTo(Location.ITALY);
                                    break;
                                case "france":
                                    rt.setTo(Location.FRANCE);
                                default:
                                    if (line.length() != 0){
                                        throw new WrongInputException("Неверно введены данные. Повторите попытку");
                                    }
                                    rt.setTo(Location.NULL);
                            }
                            break;
                        } catch (WrongInputException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
            rt.setDistance(calculateDistance(rt.getCoordinates(), rt.getTo().getCoordinates()));
            System.out.println(rt.getDistance());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rt;
    }

    /**
     * Обрабатывает файл формата CSV
     */
    private void readCSVFile(String path) {
        String[] fields = new String[]{"id", "name", "coordinates", "creationDate", "from", "to", "distance"};
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line = "";
            while ((line = br.readLine()) != null) {
                line.replaceAll("\r\n", "");
                Route rt = new Route();
                String[] arr = line.split(",");
                //System.out.println(arr[0] + " " + arr[1] + " " + arr[2] + " " + arr[3] + " " + arr[4] + " " + arr[5] + " " + arr[6]);
//                for (String i : arr){
//                    System.out.println(i);
//                }
                for (int i = 0; i < 7; i++) {
                    switch (i) {
                        case 0 -> {
                            int id = Integer.parseInt(arr[0]);
                            rt.setId(id);
                            break;
                        }
                        case 1 -> {
                            rt.setName(arr[1]);
                            break;
                        }
                        case 2 -> {
                            String[] coord = arr[2].split(" ");
                            Coordinates coordinates = new Coordinates(Long.parseLong(coord[0]), Double.parseDouble(coord[1]), Double.parseDouble(coord[2]));
                            rt.setCoordinates(coordinates);
                            break;
                        }
                        case 4 -> {
                            arr[4] = arr[4].toLowerCase();
                            switch (arr[4]) {
                                case "russia":
                                    rt.setFrom(Location.RUSSIA);
                                    break;
                                case "usa":
                                    rt.setFrom(Location.USA);
                                    break;
                                case "norway":
                                    rt.setFrom(Location.NORWAY);
                                    break;
                                case "italy":
                                    rt.setFrom(Location.ITALY);
                                    break;
                                case "france":
                                    rt.setFrom(Location.FRANCE);
                                    break;
                                default:
                                    throw new WrongInputException("Неправильные данные");
                            }
                            break;
                        }
                        case 5 -> {
                            arr[5] = arr[5].toLowerCase();
                            switch (arr[5]) {
                                case "russia":
                                    rt.setTo(Location.RUSSIA);
                                    break;
                                case "usa":
                                    rt.setTo(Location.USA);
                                    break;
                                case "norway":
                                    rt.setTo(Location.NORWAY);
                                    break;
                                case "italy":
                                    rt.setTo(Location.ITALY);
                                    break;
                                case "france":
                                    rt.setTo(Location.FRANCE);
                                    break;
                                default:
                                    throw new WrongInputException("Неправильные данные");
                            }
                            break;
                        }
                        case 6 -> {
                            arr[6].trim();
                            Double dist = Double.parseDouble(arr[6]);
                            rt.setDistance(dist);
                            break;
                        }
                    }
                }
                add(rt);
                System.out.println(rt.toString());
            }
        } catch (IOException | WrongInputException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void input() {
        String line = "";
        Route rt = new Route();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Введите путь до файла с данными");
        try {
            path = br.readLine();
            readCSVFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(true) {
            try {
                while (true) {
                    line = br.readLine();
                    line.replaceAll("\n", "");
                    line.trim();
                    String[] ln = line.split(" ");
                    switch (ln[0]){
                        case "help":
                            help();
                            break;
                        case "info":
                            info();
                            break;
                        case "show":
                            System.out.println(show());
                            break;
                        case "add":
                            rt = getRoute(br);
                            add(rt);
                            break;
                        case "update":
                            rt = getRoute(br);
                            update(Integer.parseInt(ln[1]), rt);
                            break;
                        case "remove_by_id":
                            remove_by_id(Integer.parseInt(ln[1]));
                            break;
                        case "clear":
                            clear();
                            break;
                        case "save":
                            save();
                            break;
                        case "exit":
                            exit();
                            break;
                        case "add_if_max":
                            rt = getRoute(br);
                            add_if_max(rt);
                            break;
                        case "add_if_min":
                            rt = getRoute(br);
                            add_if_min(rt);
                            break;
                        case "remove_lower":
                            rt = getRoute(br);
                            remove_lower(rt);
                            break;
                        case "min_by_id":
                            min_by_id();
                            break;
                        case "group_counting_by_distance":
                            group_counting_by_distance();
                            break;
                        case "count_by_distance":
                            count_by_distance(Long.parseLong(ln[1]));
                            break;
                        case "execute_script":
                            execute_script(ln[1]);
                            break;
                        default:
                            throw new WrongInputException("Введена неверная команда. Повторите ввод.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            } catch (WrongInputException e) {
                e.printStackTrace();
                continue;
            }
        }
    }
}
