/* Я для винды написал батник и в консоли играется очень иммерсивно (работает cls) не
   хватает только AR но для мака я не знаю как это написать :(
   Однако есть проблема (UTF-8 пишет но почему-то введенное имя на кириллице не считывается)
   но тут не работает cls */

import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;

/* Какие символы вообще могут быть на поле */
enum Symbol {
    SHIP('█'),
    SHIP_HIT('X'),
    WATER_MISS('·'),
    WATER('░');
    final char value;
    
    Symbol(char value) {
        this.value = value;
    }
    
    public char getValue() {
        return value;
    }
}

/* Класс который хранит координаты конкретной точки на поле боя */
class Position /* implements Cloneable */ {
    public int r;
    public int c;
    
    Position(int r, int c) {
        this.r = r;
        this.c = c;
    }
    
    // @Override
    // public Position clone() {
    //     try {
    //         Position clone = (Position) super.clone();
    //         // copy mutable state here, so the clone can't change the internals of the original
    //         return clone;
    //     } catch (CloneNotSupportedException e) {
    //         throw new AssertionError();
    //     }
    // }
}

/* Одна ячейка поля битвы */
class Cell {
    /* Какой символ отображать на поле для этой клетки */
    Symbol symbol;
    /* Отвечает за то, можно ли размещать тайл корабля на данной клетке */
    boolean isShipTilePlaceable;
}

/* Собственно поле битвы */
class Field {
    int size;
    Cell[][] cells;
    int shipTilesNum;
    
    Field(int fieldSize) {
        this.size = fieldSize;
        this.cells = new Cell[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j] = new Cell();
                cells[i][j].isShipTilePlaceable = true;
                cells[i][j].symbol = Symbol.WATER;
            }
        }
        this.shipTilesNum = 0;
    }
    
    /* Размещаем следующие корабли:
       Один четырехпалубный
       Два трехпалубных
       Три двухпалубных
       Четыре однопалубных */
    void placeShips(Random rand) {
        placeShip(4, rand);
        shipTilesNum += 4;
        for (int i = 0; i < 2; i++) {
            placeShip(3, rand);
            shipTilesNum += 3;
        }
        for (int i = 0; i < 3; i++) {
            placeShip(2, rand);
            shipTilesNum += 2;
        }
        for (int i = 0; i < 4; i++) {
            placeShip(1, rand);
            shipTilesNum += 1;
        }
    }
    
    /* Размещает на поле сгенерированный в generateShip корабль */
    boolean placeShip(int shipLength, Random rand) {
        Position[] tiles = generateShip(shipLength, rand);
        if (tiles == null) {
            return false;
        }
        for (Position tile : tiles) {
            setFieldSymbol(tile, Symbol.SHIP);
            setCellPlaceable(tile, false);
            makeCellsUnplaceable(tile);
        }
        return true;
    }
    
    /* Находит точку, в которой разместить корабль. Рекурсия ради рекурсии */
    Position findFreePosition(Position pos, Random rand) {
        if (isCellValidPlaceable(pos)) {
            return pos;
        } else {
            int r = rand.nextInt(0, 10);
            int c = rand.nextInt(0, 10);
            // System.out.println("r: "+r+" c: "+c);
            return findFreePosition(new Position(r, c), rand);
        }
    }
    
    /* Сгенерировать корабль для данной точки */
    Position[] generateShip(int shipLength, Random rand) {
        /* Этот массив будет сохранять в себе позиции тайлов корабля,
           чтобы потом я мог заблокировать окружающие клетки чтобы корабли не касались друг друга */
        Position[] recordedShipPositions = new Position[shipLength];
        int recordedIndex = 0;
        Position ourPos = findFreePosition(new Position(0, 0), rand);
        // setFieldSymbol(ourPos, Symbol.SHIP);
        /* Мы вынуждены помечать устанавливаемость ячеек как false потому что иначе при поиске
           смежной ячейки мы можем выбрать уже найденный тайл корабля */
        setCellPlaceable(ourPos, false);
        recordedShipPositions[recordedIndex++] = ourPos;
        for (int i = 0; i < shipLength - 1; i++) {
            Position[] poss = findFreeAdjacentCells(ourPos);
            if (poss.length > 0) {
                ourPos = poss[rand.nextInt(poss.length)];
                // setFieldSymbol(ourPos, Symbol.SHIP);
                setCellPlaceable(ourPos, false);
                recordedShipPositions[recordedIndex++] = ourPos;
            } else {
                // System.out.println("Не удалось разместить корабль!");
                recordedShipPositions = null;
                break;
            }
        }
        /* Теперь нужно почистить то что мы пометили на поле */
        if (recordedShipPositions != null) {
            for (Position position : recordedShipPositions) {
                setCellPlaceable(position, true);
            }
        }
        return recordedShipPositions;
    }
    
    /* Находит все возможные свободные смежные с аргументом позиции
       которые строго слева, справа, сверху или снизу от позиции аргумента */
    Position[] findFreeAdjacentCells(Position pos) {
        Position[] positions = new Position[4];
        /* Мы вынуждены отслеживать позицию в массиве, это своебразный аналог append */
        int numPos = 0;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                /* Эта проверка проверяет чтобы линия пересекающая аргумент была либо вертикальной,
                   либо горизонтальной но не диагональной */
                if (i == 0 || j == 0) {
                    /* Если мы не в центре. Можно еще проверить pos != derivedPos */
                    if (!(i == 0 && j == 0)) {
                        Position derivedPos = new Position(pos.r + i, pos.c + j);
                        if (isCellWithinBorders(derivedPos)) {
                            if (cells[derivedPos.r][derivedPos.c].isShipTilePlaceable) {
                                positions[numPos++] = derivedPos;
                            }
                        }
                    }
                }
            }
        }
        /* Теперь если в positions есть элементы, надо исключить null */
        Position[] resultPositions = null;
        if (numPos > 0) {
            resultPositions = new Position[numPos];
            int resultIndex = 0;
            for (Position position : positions) {
                if (position != null) {
                    resultPositions[resultIndex++] = position;
                }
            }
        }
        positions = null; // Для сборщика мусора, возможно это полезно
        /* Если пустой, вернет null */
        return resultPositions;
    }
    
    /* ДИАГНОСТИКА Отобразить экземпляр поля */
    void display() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.print(cells[i][j].symbol.getValue() + " ");
            }
            System.out.println();
        }
    }
    
    /* ДИАГНОСТИКА Показать значения isShipTilePlaceable ячеек данного поля */
    void displayPlaceables() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (cells[i][j].isShipTilePlaceable) {
                    System.out.print("0 ");
                } else {
                    System.out.print("1 ");
                }
            }
            System.out.println();
        }
    }
    
    /* Задать символ для ячейки поля */
    void setFieldSymbol(Position pos, Symbol symbol) {
        cells[pos.r][pos.c].symbol = symbol;
    }
    
    /* Задает можно ли в данном тайле размещать палубы кораблей */
    void setCellPlaceable(Position pos, boolean placeable) {
        cells[pos.r][pos.c].isShipTilePlaceable = placeable;
    }
    
    /* Функция запрещает размещение палуб кораблей в координатах, смежных с аргументом
       чтобы корабли не касались друг друга */
    void makeCellsUnplaceable(Position pos) {
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                Position derivedPos = new Position(pos.r + i, pos.c + j);
                if (isCellWithinBorders(derivedPos)) {
                    if (cells[derivedPos.r][derivedPos.c].isShipTilePlaceable) {
                        cells[derivedPos.r][derivedPos.c].isShipTilePlaceable = false;
                    }
                }
            }
        }
    }
    
    /* Функция проверяет что данная позиция находится в пределах поля, в таком случае -> true */
    boolean isCellWithinBorders(Position pos) {
        if (pos.r < 0) return false;
        if (pos.r >= size) return false;
        if (pos.c < 0) return false;
        if (pos.c >= size) return false;
        return true;
    }
    
    /* Можно ли разместить палубу в данной позиции */
    public boolean isCellValidPlaceable(Position pos) {
        if (!isCellWithinBorders(pos)) return false;
        return cells[pos.r][pos.c].isShipTilePlaceable;
    }
    
}

/* Класс игрока */
class Player {
    String name;
    int score;
    
    Player(String name) {
        this.name = name;
        score = 0;
    }
}

/* Класс игры */
class Game {
    private final Random rand;
    private final Scanner scanner;
    private final Field computerField;
    private final Field playerField;
    private Player human;
    private final Player computer;
    private Player currentPlayer;
    private String roundStats;
    
    Game() {
        rand = new Random();
        scanner = new Scanner(System.in);
        computerField = new Field(10);
        playerField = new Field(10);
        initFields();
        computer = new Player("Комппонент");
        /* В батнике я меняю кодировку на 65001 (UTF-8) чтобы можно было играть прямо в консоли
           Но присутствует тупая ошибка -- в консоли вывод идет UTF-8, но так как я ввожу свое имя
           в самой консоли а не в программе, Java считает что введено 0 символов,
           либо не отображает их
           Зато если латинскими символами, то норм */
        inviteHuman();
        System.out.println(human.name);
        System.out.println("Инициализация завершена.");
    }
    
    void play() {
        displayGameScreen();
        currentPlayer = human;
        
        while (human.score < playerField.shipTilesNum || computer.score < playerField.shipTilesNum) {
            roundStats = "Итоги раунда:";
            if (currentPlayer == human) {
                displayGameScreen();
                humanHits();
                computerHits();
                getch();
            } else {
                displayGameScreen();
                computerHits();
                humanHits();
            }
        }
        if (human.score == playerField.shipTilesNum) {
            System.out.println(human.name + " победил!");
        } else {
            System.out.println(computer.name + " выиграл!");
        }
    }
    
    private void getch() {
        System.out.print("Нажмите любую клавишу, чтобы перейти в следующий раунд.");
        scanner.nextLine();
    }
    
    /* Компьютер бьет рекурсивно */
    private void computerHits() {
        int r = rand.nextInt(playerField.size);
        int c = rand.nextInt(playerField.size);
        roundStats += "\n" + computer.name + " выстрелил по " + r + "&" + c;
        if (playerField.cells[r][c].symbol == Symbol.SHIP) {
            roundStats += "\n" + computer.name + " попал! Он может выстрелить еще раз!";
            playerField.cells[r][c].symbol = Symbol.SHIP_HIT;
            computer.score++;
            displayGameScreen();
            /* Рекурсия */
            computerHits();
        } else {
            roundStats += "\n" + computer.name + " промахнулся! Настал ваш черед.";
            playerField.cells[r][c].symbol = Symbol.WATER_MISS;
            displayGameScreen();
            /* Задаем следующего игрока */
            currentPlayer = human;
        }
    }
    
    /* Человек бьет рекурсивно */
    private void humanHits() {
        Pattern pattern = Pattern.compile("^\\d{2}$");
        String entry;
        do {
            System.out.print("\nВведите строку куда стрелять в формате 09, где 0 - строка, а 9 - столбец: ");
            entry = scanner.nextLine();
        } while (!pattern.matcher(entry).matches());
        
        int value = Integer.parseInt(entry);
        int r = (value - (value % 10)) / 10;
        int c = value % 10;
        roundStats += "\n" + human.name + " выстрелил по " + r + "&" + c;
        if (computerField.cells[r][c].symbol == Symbol.SHIP) {
            roundStats += "\nПрофит! Вы можете выстрелить еще раз!";
            computerField.cells[r][c].symbol = Symbol.SHIP_HIT;
            human.score++;
            displayGameScreen();
            /* Рекурсия */
            humanHits();
        } else if (computerField.cells[r][c].symbol == Symbol.WATER) {
            roundStats += "\nК сожалению, вы промахнулись! Очередь компьютера";
            computerField.cells[r][c].symbol = Symbol.WATER_MISS;
            displayGameScreen();
            /* Задаем следующего игрока */
            currentPlayer = computer;
        }
    }
    
    void displayGameScreen() {
        cls();
        displayTwoFields(computerField, playerField);
        if (roundStats != null) System.out.println(roundStats);
        // System.out.printf("Компьютер: %d очков \t\t  %s: %d очков (из %d возможных)\n", computer.score, human.name, human.score, playerField.shipTilesNum);
        System.out.printf("%s шанс победы: %.1f%%\t %s шанс победы: %.1f%%\n",
                computer.name,
                (double) computer.score / computerField.shipTilesNum * 100,
                human.name,
                (double) human.score / computerField.shipTilesNum * 100
        );
    }
    
    /* Очистка экрана */
    private void cls() {
        try {
            Process p = new ProcessBuilder("cmd", "/c", "cls").inheritIO().start();
            /* Вот я не знаю, этот сегмент надо ли если по сути start уже запустился
               а если я пишу .waitFor() в конце, то среда ругается что исключение не обработано
               и мне не очень хочется вручную добавлять и удалять throws XXX по всему листингу
               каждый раз когда я что-то меняю */
            try {
                p.waitFor();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /* Приглашение человеческому игроку с просьбой ввести имя */
    private void inviteHuman() {
        cls();
        System.out.println("Добро пожаловать в игру «Морской Бой»!");
        String name = getHumanPlayerName();
        human = new Player(name);
        // human = new Player("Человек");
    }
    
    /* Получаем имя игрока-человека из консоли, если пустое то задаем имя по умолчанию */
    private String getHumanPlayerName() {
        String message = "Пожалуйста, введите ваше имя: ";
        System.out.print(message);
        String name = scanner.nextLine();
        if (!name.isBlank()) {
            return name;
        } else {
            return "Человек";
        }
    }
    
    /* Пробуем заполнить поля кораблями */
    private void initFields() {
        try {
            computerField.placeShips(rand);
        } catch (Exception e) {
            System.out.println("Не удалось создать корабли. Пожалуйста, перезагрузите игру.");
            System.exit(1);
        }
        try {
            playerField.placeShips(rand);
        } catch (Exception e) {
            System.out.println("Не удалось создать корабли. Пожалуйста, перезагрузите игру.");
            System.exit(1);
        }
    }
    
    /* Отображаем два поля слева направо, слева компьютер, справа человек */
    private void displayTwoFields(Field leftField, Field rightField) {
        /* А вообще сначала нужно отобразить какое поле чье */
        System.out.printf("  %-20s      %-20s\n", computer.name, human.name);
        
        int size = leftField.size;
        String separatorFields = "      ";
        
        /* Сначала надо отобразить цифры легенды левого поля */
        System.out.print("  "); // Отступ
        for (int j = 0; j < size; j++) {
            System.out.print(j + " ");
        }
        /* Сепаратор */
        System.out.print(separatorFields);
        /* Теперь отобразить цифры легенды правого поля */
        for (int j = 0; j < size; j++) {
            System.out.print(j + " ");
        }
        System.out.println();
        
        /* А теперь сами два поля. Левое поле интерпретируется как компьютерное
           так что на нем будут только хиты и миссы */
        for (int i = 0; i < size; i++) {
            /* Сначала индекс элемента */
            System.out.print(i + " ");
            /* Левое (компьютерное) */
            Symbol symbol;
            for (int j = 0; j < size; j++) {
                symbol = leftField.cells[i][j].symbol;
                if (symbol == Symbol.SHIP_HIT || symbol == Symbol.WATER_MISS) {
                    System.out.print(symbol.getValue() + " ");
                } else {
                    System.out.print(symbol.getValue() + " ");
                }
            }
            /* Сепаратор */
            System.out.print(separatorFields + "\b\b");
            System.out.print(i + " ");
            /* Правое (человек) */
            for (int j = 0; j < size; j++) {
                symbol = rightField.cells[i][j].symbol;
                System.out.print(symbol.getValue() + " ");
            }
            System.out.println();
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Game game = new Game();
        game.play();
    }
}
