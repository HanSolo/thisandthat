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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;


/**
 * User: hansolo
 * Date: 2019-06-21
 * Time: 09:36
 */
public class Demo extends Application {
    private Label              header;
    private ExpandableTextArea expandableTextArea;
    private Label              footer;
    private ToggleButton       toggleButton;
    private Button             button1;
    private Button             button2;
    private HBox               buttonBox;

    @Override public void init() {
        final String text = "";
        header             = new Label("Header");
        expandableTextArea = new ExpandableTextArea(text, true, 3, 100);
        footer             = new Label("Footer");
        toggleButton       = new ToggleButton("Not Expandable");
        button1            = new Button("Cancel");
        button2            = new Button("Ok");
        buttonBox          = new HBox(5, toggleButton, button1, button2);

        toggleButton.selectedProperty().addListener(o -> {
            if (toggleButton.isSelected()) {
                toggleButton.setText("Expandable");
                expandableTextArea.setExpandable(true);
            } else {
                toggleButton.setText("Not Expandable");
                expandableTextArea.setExpandable(false);
            }
        });

        toggleButton.setSelected(expandableTextArea.isExpandable());
    }

    @Override public void start(Stage stage) {
        VBox pane = new VBox(10, header, expandableTextArea, footer, buttonBox);

        Scene scene = new Scene(pane, 600, 400);

        stage.setTitle("ExpandableTextArea");
        stage.setScene(scene);
        stage.show();

        expandableTextArea.setText("asdfjk ölajks dfölkajs dfölkj asdöfklj asdöjkl fasödjkl földsj föladjks fölkjs dföladjs fölkjads öfkl löadjks fölkajs dfölkadjs fölkj asödfjk lökdfjs ölkadjs föladjksaldksjf aösdkjf aösdjk f ");
    }

    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
