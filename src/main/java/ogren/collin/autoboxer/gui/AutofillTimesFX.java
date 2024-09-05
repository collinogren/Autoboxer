/*
    Autoboxer to make creating "boxes" for figure skating competitions easier.
    Copyright (C) 2024 Collin Ogren

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ogren.collin.autoboxer.gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ogren.collin.autoboxer.process.ScheduleElement;
import ogren.collin.autoboxer.utilities.errordetection.BoxError;

import javax.print.attribute.standard.PrinterName;
import java.util.ArrayList;

// Dialog box for letting the user copy start and end times up or down. Useful especially for 104s that use merged cells
public class AutofillTimesFX {

    private enum Direction {
        DOWN,
        UP,
    }

    private static class Autofill {
        private boolean enabled;
        private Direction direction;

        public Autofill(boolean enabled, Direction direction) {
            this.enabled = enabled;
            this.direction = direction;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public Direction getDirection() {
            return direction;
        }

        public static Direction directionFromUI(ToggleButton down) {
            if (down.isSelected()) {
                return Direction.DOWN;
            } else {
                return Direction.UP;
            }
        }
    }

    public static void start(TextArea textArea) {
        Stage stage = new Stage();
        stage.setResizable(true);
        stage.setWidth(275);
        stage.setHeight(160);
        stage.setTitle("Autofill Times");
        stage.getIcons().add(GUIFX.autoboxerIcon);
        stage.setResizable(false);
        VBox contents = new VBox();
        contents.setSpacing(5);
        contents.setPadding(new Insets(5, 5, 5, 5));

        HBox startTimesHBox = new HBox();
        startTimesHBox.setSpacing(5);
        CheckBox startTimesCheckbox = new CheckBox("Autofill Start Times");
        startTimesCheckbox.setSelected(true);
        startTimesCheckbox.setMaxHeight(32);
        startTimesCheckbox.setMinHeight(32);
        HBox.setHgrow(startTimesCheckbox, Priority.ALWAYS);
        ToggleButton startTimeDown = new ToggleButton("↓");
        startTimeDown.setSelected(true);
        startTimeDown.setMaxHeight(startTimesCheckbox.heightProperty().doubleValue());
        startTimeDown.setMaxWidth(startTimeDown.getMaxHeight());

        ToggleButton startTimeUp = new ToggleButton("↑");
        startTimeUp.setMaxHeight(startTimesCheckbox.heightProperty().doubleValue());
        startTimeUp.setMaxWidth(startTimeUp.heightProperty().doubleValue());
        startTimeUp.setSelected(false);
        ToggleGroup startTimeToggleGroup = new ToggleGroup();
        startTimeToggleGroup.getToggles().addAll(startTimeDown, startTimeUp);
        Pane spacer1 = new Pane();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        startTimesHBox.getChildren().addAll(startTimesCheckbox, spacer1, startTimeDown, startTimeUp);

        startTimeDown.setOnAction(event -> {
            if (!startTimeDown.isSelected() && !startTimeUp.isSelected()) {
                startTimeUp.setSelected(true);
            }
        });

        startTimeUp.setOnAction(event -> {
            if (!startTimeUp.isSelected() && !startTimeDown.isSelected()) {
                startTimeDown.setSelected(true);
            }
        });

        HBox endTimesHBox = new HBox();
        endTimesHBox.setSpacing(5);
        CheckBox endTimesCheckbox = new CheckBox("Autofill End Times");
        endTimesCheckbox.setSelected(true);
        endTimesCheckbox.setMaxHeight(32);
        endTimesCheckbox.setMinHeight(32);
        HBox.setHgrow(endTimesCheckbox, Priority.ALWAYS);
        ToggleButton endTimeDown = new ToggleButton("↓");
        endTimeDown.setSelected(false);
        endTimeDown.setMaxHeight(endTimesCheckbox.heightProperty().doubleValue());
        endTimeDown.setMaxWidth(endTimeDown.getMaxHeight());
        ToggleButton endTimeUp = new ToggleButton("↑");
        endTimeUp.setMaxHeight(endTimesCheckbox.heightProperty().doubleValue());
        endTimeUp.setMaxWidth(endTimeUp.heightProperty().doubleValue());
        endTimeUp.setSelected(true);
        ToggleGroup endTimeToggleGroup = new ToggleGroup();
        endTimeToggleGroup.getToggles().addAll(endTimeDown, endTimeUp);
        Pane spacer2 = new Pane();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        endTimesHBox.getChildren().addAll(endTimesCheckbox, spacer2, endTimeDown, endTimeUp);

        endTimeDown.setOnAction(event -> {
            if (!endTimeDown.isSelected() && !endTimeUp.isSelected()) {
                endTimeUp.setSelected(true);
            }
        });

        endTimeUp.setOnAction(event -> {
            if (!endTimeUp.isSelected() && !endTimeDown.isSelected()) {
                endTimeDown.setSelected(true);
            }
        });

        contents.getChildren().addAll(startTimesHBox, endTimesHBox);

        Pane verticalSpacer = new Pane();
        verticalSpacer.setMinHeight(0);
        VBox.setVgrow(verticalSpacer, Priority.ALWAYS);

        contents.getChildren().add(verticalSpacer);

        HBox closeBox = new HBox();
        closeBox.setSpacing(5);
        Button applyButton = new Button("Apply");
        applyButton.setMaxWidth(Double.MAX_VALUE);
        Button cancelButton = new Button("Cancel");
        cancelButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setOnAction(e -> stage.close());
        applyButton.setOnAction(e -> {
            autofill(textArea, new Autofill(startTimesCheckbox.isSelected(), Autofill.directionFromUI(startTimeDown)), new Autofill(endTimesCheckbox.isSelected(), Autofill.directionFromUI(endTimeDown)));
            stage.close();
        });
        closeBox.getChildren().addAll(cancelButton, applyButton);
        HBox.setHgrow(applyButton, Priority.ALWAYS);
        HBox.setHgrow(cancelButton, Priority.ALWAYS);
        contents.getChildren().add(closeBox);

        Scene scene = new Scene(contents);
        stage.setScene(scene);
        stage.show();
    }

    private static void autofill(TextArea textArea, Autofill startTimeAutofill, Autofill endTimeAutofill) {
        String text = textArea.getText();
        String[] lines = text.split("\n");
        ArrayList<ScheduleElement> elements = new ArrayList<>();
        for (String line : lines) {
            String[] splits = line.split("\t");
            int numSplits = splits.length;
            String eventNumber = null;
            if (numSplits >= 1) {
               eventNumber = splits[0];
               if (eventNumber.trim().isEmpty()) {
                   eventNumber = null;
               }
            }

            String startTime = null;
            if (numSplits >= 2) {
               startTime = splits[1];
               if (startTime.trim().isEmpty()) {
                   startTime = null;
               }
            }

            String endTime = null;
            if (numSplits >= 3) {
                endTime = splits[2];
                if (endTime.trim().isEmpty()) {
                    endTime = null;
                }
            }

            elements.add(new ScheduleElement(eventNumber, null, startTime, endTime, null, null));
        }

        if (startTimeAutofill.enabled) {
            if (startTimeAutofill.direction == Direction.DOWN) {
                String lastKnownTime = "";
                for (int i = 0; i < elements.size(); i++) {
                    if (elements.get(i).getEventNumber() != null) {
                        if (elements.get(i).getStartTime() != null) {
                            lastKnownTime = elements.get(i).getStartTime();
                        } else {
                            elements.get(i).setStartTime(lastKnownTime);
                        }
                    }
                }
            }

            if (startTimeAutofill.direction == Direction.UP) {
                String lastKnownTime = "";
                for (int i = elements.size() - 1; i >= 0; i--) {
                    if (elements.get(i).getEventNumber() != null) {
                        if (elements.get(i).getStartTime() != null) {
                            lastKnownTime = elements.get(i).getStartTime();
                        } else {
                            elements.get(i).setStartTime(lastKnownTime);
                        }
                    }
                }

            }
        }

        if (endTimeAutofill.enabled) {
            if (endTimeAutofill.direction == Direction.DOWN) {
                String lastKnownTime = "";
                for (int i = 0; i < elements.size(); i++) {
                    if (elements.get(i).getEventNumber() != null) {
                        if (elements.get(i).getEndTime() != null) {
                            lastKnownTime = elements.get(i).getEndTime();
                        } else {
                            elements.get(i).setEndTime(lastKnownTime);
                        }
                    }
                }
            }

            if (endTimeAutofill.direction == Direction.UP) {
                String lastKnownTime = "";
                for (int i = elements.size() - 1; i >= 0; i--) {
                    if (elements.get(i).getEventNumber() != null) {
                        if (elements.get(i).getEndTime() != null) {
                            lastKnownTime = elements.get(i).getEndTime();
                        } else {
                            elements.get(i).setEndTime(lastKnownTime);
                        }
                    }
                }
            }
        }

        String newText = createScheduleText(elements);

        textArea.setText(newText);
    }

    private static String createScheduleText(ArrayList<ScheduleElement> elements) {
        StringBuilder newText = new StringBuilder();
        for (ScheduleElement element : elements) {
            if (element.getEventNumber() != null) {
                newText.append(element.getEventNumber());
            }
            newText.append("\t");

            if (element.getStartTime() != null) {
                newText.append(element.getStartTime());
            }
            newText.append("\t");

            if (element.getEndTime() != null) {
                newText.append(element.getEndTime());
            }
            newText.append("\n");
        }
        return newText.toString();
    }
}
