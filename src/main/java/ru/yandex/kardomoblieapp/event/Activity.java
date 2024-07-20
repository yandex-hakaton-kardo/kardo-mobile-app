package ru.yandex.kardomoblieapp.event;

public enum Activity {
    BMX("BMX"),
    BRAKING("Брейкинг"),
    WORKOUT("Воркаут"),
    GRAFFITI("Граффити"),
    DJ("Диждеинг"),
    PARKOUR("Паркур"),
    SKATE("Скейтбординг"),
    SCOOTER("Трюковой самоткат"),
    TRICKING("Трикинг"),
    FREERUN("Фриран"),
    HIP_HOP("Хип-хоп");

    private final String activity;
    Activity(String activity) {
        this.activity = activity;
    }


    @Override
    public String toString() {
        return this.activity;
    }
}
