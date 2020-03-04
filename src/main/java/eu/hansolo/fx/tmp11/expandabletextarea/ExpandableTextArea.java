/*
 * Copyright (c) 2020 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.tmp11.expandabletextarea;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;


public class ExpandableTextArea extends StackPane {
    private final Character       ENTER = (char) 10;
    private       int             maxNoOfCharacters;
    private       double          lineHeight;
    private       Label           label;
    private       BooleanBinding  showing;
    private       BooleanProperty fixedHeight;
    private       BooleanProperty expandable;
    private       IntegerProperty compactNoOfLines;
    private       IntegerProperty expandedNoOfLines;
    private       StackPane       labelPane;
    private       TextArea        textArea;
    private       TextAreaSkin    textAreaSkin;


    // ******************** Constructors *******************************
    public ExpandableTextArea() {
        this("", true, false, 2, Integer.MAX_VALUE);
    }
    public ExpandableTextArea(final String text) {
        this(text, true, false, 2, Integer.MAX_VALUE);
    }
    public ExpandableTextArea(final String text, final int compactNoOfLines) {
        this(text, true, false, compactNoOfLines, Integer.MAX_VALUE);
    }
    public ExpandableTextArea(final String text, final boolean expandable, final int compactNoOfLines) {
        this(text, expandable, false, compactNoOfLines, Integer.MAX_VALUE);
    }
    public ExpandableTextArea(final String text, final boolean expandable, final int compactNoOfLines, final int maxNoOfCharacters) {
        this(text, expandable, false, compactNoOfLines, maxNoOfCharacters);
    }
    public ExpandableTextArea(final String text, final boolean expandable, final boolean fixedHeight) {
        this(text, expandable, fixedHeight, 2, Integer.MAX_VALUE);
    }
    public ExpandableTextArea(final String text, final boolean expandable, final boolean fixedHeight, final int compactNoOfLines) {
        this(text, expandable, fixedHeight, compactNoOfLines, Integer.MAX_VALUE);
    }
    public ExpandableTextArea(final String text, final boolean expandable, final boolean fixedHeight, final int compactNoOfLines, final int maxNoOfCharacters) {
        super();

        this.maxNoOfCharacters = clamp(5, Integer.MAX_VALUE, maxNoOfCharacters);
        this.lineHeight        = 17;
        this.fixedHeight       = new BooleanPropertyBase(fixedHeight) {
            @Override protected void invalidated() { updateHeight(textArea.getText()); }
            @Override public Object getBean() { return ExpandableTextArea.this; }
            @Override public String getName() { return "fixedHeight"; }
        };
        this.expandable        = new BooleanPropertyBase(expandable) {
            @Override protected void invalidated() {
                if (get()) {
                        /* old approach: set to preferred height
                        label.setMaxHeight(-1);
                        textArea.setMaxHeight(-1);
                        */
                    // new approach: set to defined height
                    setToExpandedHeight();
                    //
                    textArea.setVisible(true);
                    textArea.setManaged(true);
                } else {
                    setToFixedHeight();
                    textArea.setVisible(false);
                    textArea.setManaged(false);
                }
                updateHeight(textArea.getText());
            }
            @Override public Object getBean() { return ExpandableTextArea.this; }
            @Override public String getName() { return "expandable"; }
        };
        this.compactNoOfLines  = new IntegerPropertyBase(compactNoOfLines) {
            @Override protected void invalidated() { updateHeight(textArea.getText()); }
            @Override public Object getBean() { return ExpandableTextArea.this; }
            @Override public String getName() { return "noOfRows"; }
        };
        this.expandedNoOfLines = new IntegerPropertyBase(1) {
            @Override public Object getBean() { return ExpandableTextArea.this; }
            @Override public String getName() { return "expandedNoOfLines"; }
        };

        initGraphics(text);
        registerListeners();
        setupBindings();
    }


    // ******************** Initialization ************************************
    private void initGraphics(final String text) {
        getStylesheets().add(ExpandableTextArea.class.getResource("expandable-text-area.css").toExternalForm());
        getStyleClass().add("expandable-text-area");

        setAlignment(Pos.TOP_LEFT);

        textArea = new TextArea(text);
        textArea.setPrefHeight(getCompactNoOfLines() * lineHeight);
        textArea.setWrapText(true);
        textArea.setVisible(!isFixedHeight());

        textArea.setTextFormatter(new TextFormatter<String>(change -> {
            int noOfCharacters = change.getControlNewText().length();
            if (noOfCharacters >= maxNoOfCharacters) {
                String allowedText = change.getControlNewText().substring(0, maxNoOfCharacters - 1);
                change.setText(allowedText);
                change.setRange(0, change.getControlText().length());
            }
            return change;
        }));

        label = new Label(text);
        label.setAlignment(Pos.TOP_LEFT);
        label.setPrefWidth(Double.MAX_VALUE);
        label.setPrefHeight(getCompactNoOfLines() * lineHeight);
        label.setWrapText(true);

        labelPane = new StackPane(label);
        labelPane.getStyleClass().add("label-pane");
        labelPane.setPrefWidth(Double.MAX_VALUE);
        labelPane.setAlignment(Pos.TOP_LEFT);
        labelPane.setPadding(new Insets(5, 6, 6, 9));

        getChildren().addAll(labelPane, textArea);
    }

    private void registerListeners() {
        textArea.widthProperty().addListener(o -> updateHeight(textArea.getText()));
        textArea.textProperty().addListener(o -> updateHeight(textArea.getText()));
        label.heightProperty().addListener(o -> updateHeight(textArea.getText()));
    }

    private void setupBindings() {
        textArea.mouseTransparentProperty().bind(mouseTransparentProperty());

        label.prefWidthProperty().bind(textArea.widthProperty());
        label.textProperty().bind(new StringBinding() {
            {
                bind(textArea.textProperty());
            }

            @Override protected String computeValue() {
                if (null != textArea.getText() && textArea.getText().length() > 0) {
                    if (!((Character) textArea.getText().charAt(textArea.getText().length() - 1)).equals(ENTER)) {
                        return textArea.getText() + ENTER;
                    }
                }
                return textArea.getText();
            }
        });

        // Binding the container width/height to the TextArea width.
        labelPane.maxWidthProperty().bind(textArea.widthProperty());

        if (null != getScene()) {
            initBinding();
        } else {
            sceneProperty().addListener((o1, ov1, nv1) -> {
                if (null == nv1) { return; }
                if (null != getScene().getWindow()) {
                    initBinding();
                } else {
                    sceneProperty().get().windowProperty().addListener((o2, ov2, nv2) -> {
                        if (null == nv2) { return; }
                        initBinding();
                    });
                }
            });
        }
    }


    // ******************** Methods *******************************************
    public boolean isFixedHeight() { return fixedHeight.get(); }
    public void setFixedHeight(final boolean fixedHeight) { this.fixedHeight.set(fixedHeight); }
    public BooleanProperty fixedHeightProperty() { return fixedHeight; }

    public boolean isExpandable() { return expandable.get(); }
    public void setExpandable(final boolean expandable) { this.expandable.set(expandable); }
    public BooleanProperty expandableProperty() { return expandable; }

    public int getCompactNoOfLines() { return compactNoOfLines.get(); }
    public void setCompactNoOfLines(final int compactNoOfLines) { this.compactNoOfLines.set(compactNoOfLines); }
    public IntegerProperty compactNoOfLinesProperty() { return compactNoOfLines; }

    public TextArea getTextArea() { return textArea; }

    public String getText() { return textArea.getText(); }
    public void setText(final String text) { textArea.setText(text); }
    public StringProperty textProperty() { return textArea.textProperty(); }

    public boolean isEditable() { return textArea.isEditable(); }
    public void setEditable(final boolean editable) { textArea.setEditable(editable); }
    public BooleanProperty editableProperty() { return textArea.editableProperty(); }

    public int getExpandedNoOfLines() { return expandedNoOfLines.get(); }
    public ReadOnlyIntegerProperty expandedNoOfLinesProperty() { return expandedNoOfLines; }

    private void updateHeight(final String text) {
        if (isExpandable() && isFocused()) {
            int textLength = text.length();
            if (textLength < 1) {
                expandedNoOfLines.set(1);
            } else {
                Rectangle2D startBounds = textAreaSkin.getCharacterBounds(1);
                Rectangle2D endBounds   = textAreaSkin.getCharacterBounds(text.length());
                if (null == startBounds || null == endBounds) {
                    expandedNoOfLines.set(1);
                } else {
                    lineHeight = endBounds.getHeight();
                    expandedNoOfLines.set(clamp(1, Integer.MAX_VALUE, (int) ((endBounds.getMaxY() - (null == startBounds ? 0 : startBounds.getMinY())) / (lineHeight - 2))));
                }
            }
            setToExpandedHeight();
        }
    }

    private void initBinding() {
        showing = Bindings.createBooleanBinding(() -> {
            if (getScene() != null && getScene().getWindow() != null) {
                return getScene().getWindow().isShowing();
            } else {
                return false;
            }
        }, sceneProperty(), getScene().windowProperty(), getScene().getWindow().showingProperty());

        showing.addListener((o, ov, nv) -> {
            if (nv) {
                ScrollBar verticalScrollBar = (ScrollBar) lookup(".scroll-bar:vertical");
                if (null != verticalScrollBar) { verticalScrollBar.setDisable(true); }
                textAreaSkin = (TextAreaSkin) textArea.getSkin();
            }
        });
    }

    private int clamp(final int min, final int max, final int value) {
        if (value < min) { return min; }
        if (value > max) { return max; }
        return value;
    }

    private void setToFixedHeight() {
        double height = getCompactNoOfLines() * lineHeight;
        label.setMaxHeight(height);
        textArea.setMinHeight(height);
        textArea.setMaxHeight(height);
        textArea.setPrefHeight(height);
        textArea.setPrefRowCount(getCompactNoOfLines());
        requestLayout();
    }

    private void setToExpandedHeight() {
        double fixedHeight    = getCompactNoOfLines() * lineHeight;
        double expandedHeight = getExpandedNoOfLines() * lineHeight;
        double height         = expandedHeight < fixedHeight ? fixedHeight : expandedHeight;
        label.setMaxHeight(height);
        textArea.setMinHeight(height);
        textArea.setMaxHeight(height);
        textArea.setPrefHeight(height);
        textArea.setPrefRowCount(getExpandedNoOfLines());
        requestLayout();
    }
}
