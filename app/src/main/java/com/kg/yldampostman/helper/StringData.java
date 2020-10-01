package com.kg.yldampostman.helper;

import java.util.ArrayList;
import java.util.List;

public class StringData {



    public static List<String> getProvinceList() {
        List<String> spinnerArray = new ArrayList<>();
        spinnerArray.add("");
        spinnerArray.add("Чуй");
        spinnerArray.add("Ош");
        spinnerArray.add("Баткен");
        spinnerArray.add("Жалал-Абад");
        spinnerArray.add("Нарын");
        spinnerArray.add("Ыссык-Көл");
        spinnerArray.add("Талас");
        spinnerArray.add("Башкалар");

        return spinnerArray;
    }

    public static List<String> getCityList() {
        List<String> spinnerArray = new ArrayList<>();
        spinnerArray.add("");
        spinnerArray.add("Бишкек");
        spinnerArray.add("Ош");
        spinnerArray.add("Жалал-Абад");
        spinnerArray.add("Баткен");
        spinnerArray.add("Нарын");
        spinnerArray.add("Каракол");
        spinnerArray.add("Талас");
        spinnerArray.add("Москва");
        spinnerArray.add("Кочкор-Ата");
        spinnerArray.add("Таш-Комур");
        spinnerArray.add("Чолпон-Ата");
        spinnerArray.add("Токмок");
        spinnerArray.add("Кара-Балта");
        spinnerArray.add("Сокулук");
        spinnerArray.add("Кемин");
        spinnerArray.add("Өзгөн");
        spinnerArray.add("Ноокат");
        spinnerArray.add("Кара-Суу");
        spinnerArray.add("Кадамжай");
        spinnerArray.add("Кызыл-Кыя");
        spinnerArray.add("Лейлек");
        spinnerArray.add("Сулукту");
        spinnerArray.add("Базар-Коргон");
        spinnerArray.add("Сузак");
        spinnerArray.add("Ноокен");

        return spinnerArray;
    }

    public static String getProvince(String region) {
        if (region.equalsIgnoreCase("Бишкек"))
            return "Чуй";
        else if (region.equalsIgnoreCase("Токмок"))
            return "Чуй";
        else if (region.equalsIgnoreCase("Кара-Балта"))
            return "Чуй";
        else if (region.equalsIgnoreCase("Сокулук"))
            return "Чуй";
        else if (region.equalsIgnoreCase("Кемин"))
            return "Чуй";
        else if (region.equalsIgnoreCase("Ош"))
            return "Ош";
        else if (region.equalsIgnoreCase("Өзгөн"))
            return "Ош";
        else if (region.equalsIgnoreCase("Ноокат"))
            return "Ош";
        else if (region.equalsIgnoreCase("Кара-Суу"))
            return "Ош";
        else if (region.equalsIgnoreCase("Баткен"))
            return "Баткен";
        else if (region.equalsIgnoreCase("Кадамжай"))
            return "Баткен";
        else if (region.equalsIgnoreCase("Кызыл-Кыя"))
            return "Баткен";
        else if (region.equalsIgnoreCase("Лейлек"))
            return "Баткен";
        else if (region.equalsIgnoreCase("Сулукту"))
            return "Баткен";
        else if (region.equalsIgnoreCase("Жалал-Абад"))
            return "Жалал-Абад";
        else if (region.equalsIgnoreCase("Кочкор-Ата"))
            return "Жалал-Абад";
        else if (region.equalsIgnoreCase("Таш-Комур"))
            return "Жалал-Абад";
        else if (region.equalsIgnoreCase("Базар-Коргон"))
            return "Жалал-Абад";
        else if (region.equalsIgnoreCase("Сузак"))
            return "Жалал-Абад";
        else if (region.equalsIgnoreCase("Ноокен"))
            return "Жалал-Абад";
        else if (region.equalsIgnoreCase("Нарын"))
            return "Нарын";
        else if (region.equalsIgnoreCase("Каракол"))
            return "Ыссык-Көл";
        else if (region.equalsIgnoreCase("Чолпон-Ата"))
            return "Ыссык-Көл";
        else if (region.equalsIgnoreCase("Талас"))
            return "Талас";
        else if (region.equalsIgnoreCase("Москва"))
            return "Башкалар";
        else
            return "Башкалар";
    }

    public static List<String> getCityList(String province) {
        List<String> spinnerArray = new ArrayList<>();

        if (province.equalsIgnoreCase("Чуй")) {
            spinnerArray.add("Бишкек");
            spinnerArray.add("Токмок");
            spinnerArray.add("Кара-Балта");
            spinnerArray.add("Сокулук");
            spinnerArray.add("Кемин");
            spinnerArray.add("Чуй");
        } else if (province.equalsIgnoreCase("Ош")) {
            spinnerArray.add("Ош");
            spinnerArray.add("Өзгөн");
            spinnerArray.add("Ноокат");
            spinnerArray.add("Кара-Суу");
        } else if (province.equalsIgnoreCase("Баткен")) {
            spinnerArray.add("Баткен");
            spinnerArray.add("Кадамжай");
            spinnerArray.add("Кызыл-Кыя");
            spinnerArray.add("Лейлек");
            spinnerArray.add("Сулукту");
        } else if (province.equalsIgnoreCase("Жалал-Абад")) {
            spinnerArray.add("Жалал-Абад");
            spinnerArray.add("Кочкор-Ата");
            spinnerArray.add("Таш-Көмур");
            spinnerArray.add("Кара-Көл");
            spinnerArray.add("Базар-Коргон");
            spinnerArray.add("Сузак");
            spinnerArray.add("Ноокен");
        } else if (province.equalsIgnoreCase("Нарын")) {
            spinnerArray.add("Нарын");
        } else if (province.equalsIgnoreCase("Ыссык-Көл")) {
            spinnerArray.add("Каракол");
            spinnerArray.add("Чолпон-Ата");
        } else if (province.equalsIgnoreCase("Талас")) {
            spinnerArray.add("Талас");
        } else if (province.equalsIgnoreCase("Башкалар")) {
            spinnerArray.add("Москва");
        }

        return spinnerArray;
    }

    public static ArrayList<String> getSectors(String city) {
        ArrayList<String> spinnerArray = new ArrayList<>();

        if (city.equalsIgnoreCase("Бишкек")) {
            spinnerArray.add("");
            spinnerArray.add("Джал");
            spinnerArray.add("Караван");
            spinnerArray.add("Цум");
            spinnerArray.add("Микрорайон");
            spinnerArray.add("Центр");

        } else if (city.equalsIgnoreCase("Ош")) {
            spinnerArray.add("");
            spinnerArray.add("Ош");
            spinnerArray.add("Араванский");
            spinnerArray.add("Базар");
        } else {
            spinnerArray.add("");
            spinnerArray.add(city);
        }
        return spinnerArray;
    }
}
