package logbook.internal.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ProbCalcController extends WindowController {

    @FXML
    private TextField probInput;

    @FXML
    private TextField limitInput;

    @FXML
    private TextField stepInput;

    @FXML
    private TableView<ProbRow> resultTable;

    private ObservableList<ProbRow> rows = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        this.resultTable.setItems(this.rows);
        this.probInput.textProperty().addListener((ob, o, n) -> this.update());
        this.limitInput.textProperty().addListener((ob, o, n) -> this.update());
        this.stepInput.textProperty().addListener((ob, o, n) -> this.update());
    }

    private void update() {
        this.rows.clear();
        String text = this.probInput.getText();
        if (this.limitInput == null || this.stepInput == null) return;
        String limitText = this.limitInput.getText();
        String stepText = this.stepInput.getText();
        
        if (text == null || text.isEmpty() || limitText == null || limitText.isEmpty() || stepText == null || stepText.isEmpty()) {
            return;
        }

        try {
            double probPercent = Double.parseDouble(text);
            if (probPercent <= 0 || probPercent >= 100) {
                return;
            }
            int limit = Integer.parseInt(limitText);
            int step = Integer.parseInt(stepText);

            if (limit <= 0 || step <= 0) {
                return;
            }

            double p = probPercent / 100.0;

            List<ProbRow> newRows = new ArrayList<>();


            // Always add upper limit
            double probOfAtLeastOneLimit = 1.0 - Math.pow(1.0 - p, limit);
            newRows.add(new ProbRow(limit, probOfAtLeastOneLimit));

            // Calculate for increments if step <= limit
            if (step <= limit) {
                for (int count = step; count <= limit; count += step) {
                    double probOfAtLeastOne = 1.0 - Math.pow(1.0 - p, count);
                    newRows.add(new ProbRow(count, probOfAtLeastOne));
                }
            }

            // Always add exceeding counts (50%, 75%, 90%, 95%)
            double[] targets = { 0.5, 0.75, 0.9, 0.95 };
            for (double target : targets) {
                int exceed = (int) Math.ceil(Math.log(1.0 - target) / Math.log(1.0 - p));
                double prob = 1.0 - Math.pow(1.0 - p, exceed);
                newRows.add(new ProbRow(exceed, prob));
            }

            // Sort by count
            Collections.sort(newRows, Comparator.comparingInt(ProbRow::getCount));

            // Remove duplicates
            int lastCount = -1;
            for (ProbRow row : newRows) {
                if (row.getCount() != lastCount) {
                    this.rows.add(row);
                    lastCount = row.getCount();
                }
            }

        } catch (NumberFormatException e) {
            // Ignore invalid input
        }
    }

    public static class ProbRow {
        private final int count;
        private final double prob;

        public ProbRow(int count, double prob) {
            this.count = count;
            this.prob = prob;
        }

        public int getCount() {
            return count;
        }

        public double getProb() {
            return prob;
        }

        public String getCountString() {
            return String.valueOf(count);
        }

        public String getProbString() {
            return String.format("%.1f%%", prob * 100.0);
        }
    }
}
