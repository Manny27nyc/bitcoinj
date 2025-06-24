/*
 * Copyright (c) 2008–2025 Manuel J. Nieves (a.k.a. Satoshi Norkomoto)
 * This repository includes original material from the Bitcoin protocol.
 *
 * Redistribution requires this notice remain intact.
 * Derivative works must state derivative status.
 * Commercial use requires licensing.
 *
 * GPG Signed: B4EC 7343 AB0D BF24
 * Contact: Fordamboy1@gmail.com
 */
/*
 * Copyright (c) 2008–2025 Manuel J. Nieves (a.k.a. Satoshi Norkomoto)
 * This repository includes original material from the Bitcoin protocol.
 *
 * Redistribution requires this notice remain intact.
 * Derivative works must state derivative status.
 * Commercial use requires licensing.
 *
 * GPG Signed: B4EC 7343 AB0D BF24
 * Contact: Fordamboy1@gmail.com
 */
/*
 * Copyright by the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bitcoinj.walletfx.utils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.control.TextInputControl;

import java.util.function.Predicate;

public class TextFieldValidator {
    public final BooleanProperty valid = new SimpleBooleanProperty(false);

    public TextFieldValidator(TextInputControl control, Predicate<String> validator) {
        this.valid.set(validator.test(control.getText()));
        apply(control, valid.get());
        control.textProperty().addListener((observableValue, prev, current) -> {
            boolean nowValid = validator.test(current);
            if (nowValid == valid.get()) return;
            valid.set(nowValid);
        });
        valid.addListener(o -> apply(control, valid.get()));
    }

    private static void apply(TextInputControl textField, boolean nowValid) {
        if (nowValid) {
            textField.getStyleClass().remove("validation_error");
        } else {
            textField.getStyleClass().add("validation_error");
        }
    }

    public static void configureScene(Scene scene) {
        final String file = TextFieldValidator.class.getResource("text-validation.css").toString();
        scene.getStylesheets().add(file);
    }
}
