package com.hhp227.application.seat;

public class SeatItem {
    int id;
    String name;
    int activeTotal;
    int occupied;
    int available;
    String[] disable;

    public SeatItem(int id, String name, int activeTotal, int occupied, int available, String[] disable) {
        this.id = id;
        this.name = name;
        this.activeTotal = activeTotal;
        this.occupied = occupied;
        this.available = available;
        this.disable = disable;
    }
}
