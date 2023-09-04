/* Какие символы вообще могут быть на поле */
enum Symbol {
    SHIP(' '),
    SHIP_HIT('X'),
    WATER_MISS('·'),
    WATER('~');
    final char value;

    Symbol(char value) {
        this.value = value;
    }

    public char getValue() {
        return value;
    }
}

/* Класс который хранит координаты конкретной точки на поле боя */
class Position {
    public int x;
    public int y;

    Position(int row, int column) {
        x = row;
        y = column;
    }
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
    }

    /* Размещаем следующие корабли:
       Один четырехпалубный
       Два трехпалубных
       Три двухпалубных
       Четыре однопалубных */
    static void placeShips() {
        for (int i = 0; i < 1; i++) {
            placeShip(4, new Position(5, 5));
        }
    }

    static void placeShip(int shipLength, Position pos) {

    }


    /* Отобразить экземпляр */
    void display() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.print(cells[i][j].symbol.getValue());
            }
            System.out.println();
        }
    }

    /* Задать символ для ячейки поля */
    void setFieldSymbol(Position pos, Symbol symbol) {
        cells[pos.x][pos.y].symbol = symbol;
        if (symbol == Symbol.SHIP) {
            cells[pos.x][pos.y].isShipTilePlaceable = false;
        }
    }



    /* Функция запрещает размещение палуб кораблей в координатах, смежных с аргументом */
    void makeCellUnplaceable(Position pos) {
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                Position derivedPos = new Position(pos.x + i, pos.y + j);
                if (isCellWithinBorders(derivedPos)) {
                    if (cells[derivedPos.x][derivedPos.y].isShipTilePlaceable) {
                        cells[derivedPos.x][derivedPos.y].isShipTilePlaceable = false;
                    }
                }
            }
        }
    }

    /* Функция проверяет что данная позиция находится в пределах поля, в таком случае -> true */
    boolean isCellWithinBorders(Position pos) {
        if (pos.x < 0) return false;
        if (pos.x >= size) return false;
        if (pos.y < 0) return false;
        if (pos.y >= size) return false;
        return true;
    }

    /* Можно ли разместить палубу в данной позиции */
    public boolean isCellValidPlaceable(Position pos) {
        if (!isCellWithinBorders(pos)) return false;
        return cells[pos.x][pos.y].isShipTilePlaceable;
    }

}

public class Main {
    public static void main(String[] args) {
        Field playerField = new Field(10);
        playerField.display();
    }
}