/* Какие символы вообще могут быть на поле */
enum Symbol {
    SHIP('#'),
    WATER(' ');
    final char value;

    Symbol(char value) {
        this.value = value;
    }

    public char getValue() {
        return value;
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
                cells[i][j].isShipTilePlaceable = true;
                cells[i][j].symbol = Symbol.WATER;
            }
        }
    }

    public void display() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.print(cells[i][j].symbol.getValue());
            }
            System.out.println();
        }
    }
}

public class Main {
    Field playerField = new Field(10);
    playerField.display();
}