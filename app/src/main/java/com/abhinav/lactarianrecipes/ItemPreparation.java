package com.abhinav.lactarianrecipes;

public class ItemPreparation {
    String step;
    String number;

    public ItemPreparation(String step, String number) {
        this.step = step;
        this.number = number;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getStep() {
        return step;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }
}
