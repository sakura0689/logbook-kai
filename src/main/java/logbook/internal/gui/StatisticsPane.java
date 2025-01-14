package logbook.internal.gui;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import logbook.bean.Ship;
import logbook.bean.ShipCollection;
import logbook.constants.ExpTable;
import logbook.constants.StatisticsShipTypeGroup;
import logbook.internal.logger.LoggerHolder;

/**
 * 所有艦娘一覧機能
 * 統計タブ
 *
 */
public class StatisticsPane extends VBox {

    @FXML
    private VBox content;

    /** 総計 */
    @FXML
    private Text total;

    /** 比率 */
    @FXML
    private PieChart ratio;

    /** 平均レベル */
    @FXML
    private StackedBarChart<Double, String> average;

    /** 平均レベル カテゴリ軸 */
    @FXML
    private CategoryAxis averageCategory;

    /** レベル中央値 */
    @FXML
    private StackedBarChart<Double, String> median;

    /** レベル中央値 カテゴリ軸 */
    @FXML
    private CategoryAxis medianCategory;

    /** レベル分布 */
    @FXML
    private StackedBarChart<Long, String> spectrum;

    /** レベル分布 カテゴリ軸 */
    @FXML
    private CategoryAxis spectrumCategory;

    /** レベル分布 */
    @FXML
    private BubbleChart<Double, Double> bubble;

    public StatisticsPane() {
        try {
            FXMLLoader loader = InternalFXMLLoader.load("logbook/gui/statistics.fxml");
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            LoggerHolder.get().error("FXMLのロードに失敗しました", e);
        }
    }

    @FXML
    void initialize() {
    }

    /**
     * 画像ファイルに保存
     * @param event ActionEvent
     */
    @FXML
    void storeImageAction(ActionEvent event) {
        Tools.Controls.storeSnapshot(this.content, "統計情報", this.content.getScene().getWindow());
    }

    /**
     * 更新
     */
    public void update() {
        // 対象艦抽出
        List<Ship> ships = ShipCollection.get()
                .getShipMap()
                .values()
                .stream()
                // ロックしている艦
                .filter(Ship::getLocked)
                .collect(Collectors.toList());
        // 総計
        this.setTotal(ships);
        // 経験値比率
        this.setRatio(ships);
        // 平均レベル
        this.setAverage(ships);
        // レベル中央値
        this.setMedian(ships);
        // レベル分布
        this.setSpectrum(ships);
        this.setBubble(ships);
    }

    /**
     * 総計
     * @param ships 対象艦
     */
    private void setTotal(List<Ship> ships) {
        this.total.setText(this.suffixDecimal(ships.stream()
                .mapToLong(this::getExp)
                .sum()));
    }

    /**
     * 経験値比率
     * @param ships 対象艦
     */
    private void setRatio(List<Ship> ships) {
        Map<StatisticsShipTypeGroup, Long> collect = ships.stream()
                .collect(Collectors.groupingBy(StatisticsShipTypeGroup::toTypeGroup, TreeMap::new,
                        Collectors.summingLong(this::getExp)));

        ObservableList<PieChart.Data> value = FXCollections.observableArrayList();
        for (Entry<StatisticsShipTypeGroup, Long> data : collect.entrySet()) {
            if (data.getKey() != null)
                value.add(new PieChart.Data(data.getKey().name(), data.getValue()));
        }
        this.ratio.setData(value);
    }

    /**
     * 平均レベル
     * @param ships 対象艦
     */
    private void setAverage(List<Ship> ships) {
        this.average.getData().clear();

        this.averageCategory.setCategories(
                Arrays.stream(StatisticsShipTypeGroup.values())
                        .map(StatisticsShipTypeGroup::name)
                        .collect(Collectors.toCollection(FXCollections::observableArrayList)));
        ships.stream()
                .collect(Collectors.groupingBy(StatisticsShipTypeGroup::toTypeGroup,
                        Collectors.averagingLong(Ship::getLv)))
                .entrySet().stream()
                .filter(e -> e.getKey() != null)
                .sorted(Comparator.comparing(Entry<StatisticsShipTypeGroup, Double>::getKey))
                .map(e -> new XYChart.Data<>(e.getValue(), e.getKey().name()))
                .forEach(data -> {
                    XYChart.Series<Double, String> series = new XYChart.Series<>();
                    series.setName(data.getYValue());
                    series.getData().add(data);
                    this.average.getData().add(series);
                });
    }

    /**
     * レベル中央値
     * @param ships 対象艦
     */
    private void setMedian(List<Ship> ships) {
        this.median.getData().clear();

        this.medianCategory.setCategories(
                Arrays.stream(StatisticsShipTypeGroup.values())
                        .map(StatisticsShipTypeGroup::name)
                        .collect(Collectors.toCollection(FXCollections::observableArrayList)));
        
        Map<StatisticsShipTypeGroup, List<Integer>> value = ships.stream()
                    .collect(Collectors.groupingBy(StatisticsShipTypeGroup::toTypeGroup, Collectors.mapping(Ship::getLv, Collectors.toList())));
        
        for (StatisticsShipTypeGroup group : StatisticsShipTypeGroup.values()) {
            List<Integer> levels = value.get(group);
            if (levels != null) {
                int size = levels.size();
                levels.sort(Integer::compareTo);
                double median = (size % 2 == 0)
                        ? (double) (levels.get(size / 2) + levels.get(Math.max(size / 2 - 1, 0))) / 2
                        : (double) levels.get(size / 2);

                XYChart.Series<Double, String> series = new XYChart.Series<Double, String>();
                series.setName(group.name());
                series.getData().add(new XYChart.Data<>(median, group.name()));
                this.median.getData().add(series);
            }
        }
    }

    /**
     * レベル分布
     * @param ships 対象艦
     */
    private void setSpectrum(List<Ship> ships) {
        this.spectrum.getData().clear();

        Map<StatisticsShipTypeGroup, Map<Integer, Long>> value = ships.stream()
                .collect(Collectors.groupingBy(StatisticsShipTypeGroup::toTypeGroup, Collectors.mapping(this::tickLevel,
                        Collectors.groupingBy(Function.identity(), Collectors.counting()))));
        
        List<Integer> categoriesList = IntStream.rangeClosed(1, ExpTable.MAX_LEVEL)
                .map(i -> i / 10 * 10)
                .distinct()
                .mapToObj(Integer::valueOf)
                .collect(Collectors.toList());

        List<String> categoriesLavelList = new ArrayList<String>();
        for (Integer lv : categoriesList) {
            if (lv == 90) {
                categoriesLavelList.add(Integer.toString(lv) + "-" + Integer.toString(lv + 8));
                categoriesLavelList.add(Integer.toString(99));
            } else {
                categoriesLavelList.add(Integer.toString(lv) + "-" + Integer.toString(lv + 9));
            }
        }

        this.spectrumCategory.setCategories(categoriesLavelList.stream()
                        .collect(Collectors.toCollection(FXCollections::observableArrayList)));
        
        for (StatisticsShipTypeGroup group : StatisticsShipTypeGroup.values()) {
            Map<Integer, Long> tick = value.get(group);
            if (tick != null) {
                Map<String, Long> graph_data = new HashMap<String, Long>();
                for (Entry<Integer, Long> ent : tick.entrySet()) {
                    if (ent.getKey() == 99) {
                        graph_data.put("99", ent.getValue());
                    } else if (ent.getKey() == 90) {
                        graph_data.put(Integer.toString(ent.getKey()) + "-" + Integer.toString(ent.getKey() + 8), ent.getValue());
                    } else {
                        graph_data.put(Integer.toString(ent.getKey()) + "-" + Integer.toString(ent.getKey() + 9), ent.getValue());
                    }
                }
                
                ObservableList<XYChart.Data<Long, String>> data = graph_data.entrySet()
                        .stream()
                        .map(e -> new XYChart.Data<>(e.getValue(), e.getKey()))
                        .collect(Collectors.toCollection(FXCollections::observableArrayList));

                XYChart.Series<Long, String> series = new XYChart.Series<>();
                series.setName(group.name());
                series.getData().addAll(data);
                this.spectrum.getData().add(series);
            }
        }
    }

    /**
     * レベル分布
     * @param ships 対象艦
     */
    private void setBubble(List<Ship> ships) {
        this.bubble.getData().clear();

        Map<StatisticsShipTypeGroup, List<Integer>> value = ships.stream()
                .collect(Collectors.groupingBy(StatisticsShipTypeGroup::toTypeGroup,
                        Collectors.mapping(Ship::getLv, Collectors.toList())));

        for (StatisticsShipTypeGroup group : StatisticsShipTypeGroup.values()) {
            List<Integer> levels = value.get(group);
            if (levels != null) {
                double average = levels.stream()
                        .mapToInt(Integer::intValue)
                        .average()
                        .orElse(0);
                int size = levels.size();
                levels.sort(Integer::compareTo);
                double median = (size % 2 == 0)
                        ? (double) (levels.get(size / 2) + levels.get(Math.max(size / 2 - 1, 0))) / 2
                        : (double) levels.get(size / 2);

                ObservableList<XYChart.Data<Double, Double>> datas = FXCollections.observableArrayList();
                datas.add(new XYChart.Data<>(average, median, ((double) size) / 5));
                XYChart.Series<Double, Double> series = new XYChart.Series<>(group.name(), datas);
                this.bubble.getData().add(series);
            }
        }
    }

    /**
     * 接尾辞付きの数値に変換する
     * @param v 数値
     * @return 接尾辞付きの数値 (100Mなど)
     */
    private String suffixDecimal(long v) {
        BigDecimal d;
        String prefix;
        int scale;
        if (1000_000_000L <= v) {
            d = BigDecimal.valueOf(1000_000_000L);
            prefix = "G";
            scale = 3;
        } else if (1000_000L <= v) {
            d = BigDecimal.valueOf(1000_000L);
            prefix = "M";
            scale = 2;
        } else {
            d = BigDecimal.valueOf(1000L);
            prefix = "K";
            scale = 1;
        }
        return BigDecimal.valueOf(v)
                .divide(d, scale, RoundingMode.HALF_EVEN)
                .toPlainString() + prefix;
    }

    private long getExp(Ship ship) {
        return ship.getExp().get(0);
    }

    private int tickLevel(Ship ship) {
        if (ship.getLv() == 99) {
            return ship.getLv();
        }
        return ship.getLv() / 10 * 10;
    }
}
