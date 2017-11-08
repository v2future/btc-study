/*
 *
 *  Copyright 2014 http://Bither.net
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package net.bither.viewsystem;

import net.bither.BitherUI;
import net.bither.languages.Languages;
import net.bither.languages.MessageKey;
import net.bither.utils.DocumentMaxLengthFilter;
import net.bither.viewsystem.base.AccessibilityDecorator;
import net.bither.viewsystem.components.borders.TextBubbleBorder;
import net.bither.viewsystem.themes.Themes;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collection;

/**
 * <p>Utility to provide the following to UI:</p>
 * <ul>
 * <li>Provision of localised text boxes</li>
 * </ul>
 *
 * @since 0.0.1
 */
public class TextBoxes {

    /**
     * Utilities have no public constructor
     */
    private TextBoxes() {
    }

    /**
     * @return A new text field with default theme
     */
    public static JTextField newTextField(int columns) {

        JTextField textField = new JTextField(columns);

        // Set the theme
        textField.setBorder(new TextBubbleBorder(Themes.currentTheme.dataEntryBorder()));
        textField.setBackground(Themes.currentTheme.dataEntryBackground());

        textField.setOpaque(false);

        return textField;
    }

    /**
     * @return A new text field with default theme
     */
    public static JTextField newReadOnlyTextField(int columns) {

        JTextField textField = new JTextField(columns);

        // Users should not be able to change the data
        textField.setEditable(false);

        // Set the theme
        textField.setBorder(new TextBubbleBorder(Themes.currentTheme.readOnlyBorder()));
        textField.setBackground(Themes.currentTheme.readOnlyBackground());

        textField.setOpaque(false);

        return textField;
    }

    /**
     * @param rows    The number of rows (normally 6)
     * @param columns The number of columns (normally 60)
     * @return A new read only text field with default theme
     */
    public static JTextArea newReadOnlyTextArea(int rows, int columns) {

        JTextArea textArea = new JTextArea(rows, columns);

        // Users should not be able to change the data
        textArea.setEditable(false);

        // Set the theme
        textArea.setBorder(new TextBubbleBorder(Themes.currentTheme.readOnlyBorder()));
        textArea.setBackground(Themes.currentTheme.readOnlyBackground());

        textArea.setOpaque(false);

        // Ensure line wrapping occurs correctly
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        // Ensure TAB transfers focus
        AbstractAction transferFocus = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ((Component) e.getSource()).transferFocus();
            }
        };
        textArea.getInputMap().put(KeyStroke.getKeyStroke("TAB"), "transferFocus");
        textArea.getActionMap().put("transferFocus", transferFocus);

        return textArea;

    }

    /**
     * @param rows    The number of rows (normally 6)
     * @param columns The number of columns (normally 60)
     * @return A new read only text field with default theme
     */
    public static JTextArea newTextArea(int rows, int columns) {

        JTextArea textArea = new JTextArea(rows, columns);

        // Set the theme
        textArea.setBorder(new TextBubbleBorder(Themes.currentTheme.dataEntryBorder()));
        textArea.setBackground(Themes.currentTheme.dataEntryBackground());

        textArea.setOpaque(false);

        // Ensure line wrapping occurs correctly
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        // Ensure TAB transfers focus
        AbstractAction transferFocus = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ((Component) e.getSource()).transferFocus();
            }
        };
        textArea.getInputMap().put(KeyStroke.getKeyStroke("TAB"), "transferFocus");
        textArea.getActionMap().put("transferFocus", transferFocus);

        return textArea;

    }

    /**
     * @param listener The document listener for detecting changes to the content
     * @param rows     The number of rows (normally 6)
     * @param columns  The number of columns (normally 60)
     * @return A new read only length limited text field with default theme
     */
    public static JTextArea newReadOnlyLengthLimitedTextArea(DocumentListener listener, int rows, int columns) {

        JTextArea textArea = newReadOnlyTextArea(rows, columns);

        // Limit the length of the underlying document
        DefaultStyledDocument doc = new DefaultStyledDocument();
        doc.setDocumentFilter(new DocumentMaxLengthFilter(rows * columns));
        textArea.setDocument(doc);

        // Ensure we monitor changes
        doc.addDocumentListener(listener);

        return textArea;

    }

    /**
     * @return A new "enter transaction label" text field
     */
    public static JTextField newEnterTransactionLabel() {

        JTextField textField = newTextField(BitherUI.RECEIVE_ADDRESS_LABEL_LENGTH);

        // Ensure it is accessible
        AccessibilityDecorator.apply(textField, MessageKey.TRANSACTION_LABEL, MessageKey.TRANSACTION_LABEL_TOOLTIP);

        // Limit the length of the underlying document
        DefaultStyledDocument doc = new DefaultStyledDocument();
        doc.setDocumentFilter(new DocumentMaxLengthFilter(BitherUI.RECEIVE_ADDRESS_LABEL_LENGTH));
        textField.setDocument(doc);

        return textField;
    }

    /**
     * @return A new "enter QR code label" text field
     */
    public static JTextField newEnterQRCodeLabel() {

        JTextField textField = newTextField(BitherUI.RECEIVE_ADDRESS_LABEL_LENGTH);

        // Ensure it is accessible
        AccessibilityDecorator.apply(textField, MessageKey.QR_CODE_LABEL, MessageKey.QR_CODE_LABEL_TOOLTIP);

        // Limit the length of the underlying document
        DefaultStyledDocument doc = new DefaultStyledDocument();
        doc.setDocumentFilter(new DocumentMaxLengthFilter(BitherUI.RECEIVE_ADDRESS_LABEL_LENGTH));
        textField.setDocument(doc);

        return textField;
    }

    /**
     * @return A new "enter tag" text field
     */
    public static JTextField newEnterTag() {

        JTextField textField = newTextField(20);

        // Ensure it is accessible
        AccessibilityDecorator.apply(textField, MessageKey.TAGS, MessageKey.TAGS_TOOLTIP);

        return textField;
    }

    /**
     * @return A new "enter search" text field
     */
    public static JTextField newEnterSearch() {

        JTextField textField = newTextField(60);

        // Ensure it is accessible
        AccessibilityDecorator.apply(textField, MessageKey.SEARCH, MessageKey.SEARCH_TOOLTIP);

        return textField;
    }

    /**
     * @return A new "Select file" text field
     */
    public static JTextField newSelectFile() {

        JTextField textField = newTextField(60);

        // Ensure it is accessible
        AccessibilityDecorator.apply(textField, MessageKey.SELECT_FILE, MessageKey.SELECT_FILE_TOOLTIP);

        return textField;
    }

    /**
     * @param seedTimestamp The seed timestamp to display (e.g. "1850/2")
     * @return A new "display seed timestamp" text field
     */
    public static JTextField newDisplaySeedTimestamp(String seedTimestamp) {

        JTextField textField = newReadOnlyTextField(20);

        // Ensure it is accessible
        AccessibilityDecorator.apply(textField, MessageKey.TIMESTAMP, MessageKey.TIMESTAMP_TOOLTIP);

        textField.setText(seedTimestamp);

        return textField;
    }

    /**
     * @return A new "enter seed timestamp" text field
     */
    public static JTextField newEnterSeedTimestamp() {

        JTextField textField = newTextField(20);

        // Ensure it is accessible
        AccessibilityDecorator.apply(textField, MessageKey.TIMESTAMP, MessageKey.TIMESTAMP_TOOLTIP);

        return textField;

    }

    /**
     * @param listener The document listener for detecting changes to the content
     * @param readOnly True if the field should be read only (i.e. in multi-edit mode)
     * @return A new "enter name" text field
     */
    public static JTextField newEnterName(DocumentListener listener, boolean readOnly) {

        JTextField textField = readOnly ? newReadOnlyTextField(40) : newTextField(40);

        // Ensure it is accessible
        AccessibilityDecorator.apply(textField, MessageKey.NAME, MessageKey.NAME_TOOLTIP);

        textField.getDocument().addDocumentListener(listener);

        return textField;

    }

    /**
     * @param listener The document listener for detecting changes to the content
     * @param readOnly True if the field should be read only (i.e. in multi-edit mode)
     * @return A new "enter email address" text field
     */
    public static JTextField newEnterEmailAddress(DocumentListener listener, boolean readOnly) {

        JTextField textField = readOnly ? newReadOnlyTextField(40) : newTextField(40);

        // Ensure it is accessible
        AccessibilityDecorator.apply(textField, MessageKey.EMAIL_ADDRESS, MessageKey.EMAIL_ADDRESS_TOOLTIP);

        // Detect changes
        textField.getDocument().addDocumentListener(listener);

        return textField;

    }

    public static JTextField newEnterAddress(DocumentListener listener) {

        JTextField textField = newTextField(34);

        // Ensure it is accessible
        AccessibilityDecorator.apply(textField, MessageKey.BITCOIN_ADDRESS);

        // Detect changes
        textField.getDocument().addDocumentListener(listener);

        return textField;

    }

    public static JTextField newAmount(DocumentListener listener) {

        JTextField textField = newTextField(34);

        // Ensure it is accessible
        AccessibilityDecorator.apply(textField, MessageKey.BITCOIN_ADDRESS);

        // Detect changes
        textField.getDocument().addDocumentListener(listener);

        return textField;

    }


    /**
     * @param bitcoinAddress The Bitcoin address to display
     * @return A new "display Bitcoin address" text field
     */
    public static JTextField newDisplayBitcoinAddress(String bitcoinAddress) {

        JTextField textField = newReadOnlyTextField(34);

        // Ensure it is accessible
        AccessibilityDecorator.apply(textField, MessageKey.BITCOIN_ADDRESS, MessageKey.BITCOIN_ADDRESS_TOOLTIP);

        textField.setText(bitcoinAddress);

        return textField;
    }

    /**
     * @return A new "display recipient Bitcoin addresses" multi-line text field
     */
    public static JTextArea newDisplayRecipientBitcoinAddresses() {

        // 3 rows should be sufficient to cover all transactions from us
        JTextArea textArea = newReadOnlyTextArea(3, 34);

        // Ensure it is accessible
        AccessibilityDecorator.apply(textArea, MessageKey.RECIPIENT, MessageKey.RECIPIENT_TOOLTIP);

        return textArea;
    }

    /**
     * @param listener The document listener for detecting changes to the content
     * @param readOnly True if the field should be read only (i.e. in multi-edit mode)
     * @return A new "enter extended public key" text field
     */
    public static JTextField newEnterExtendedPublicKey(DocumentListener listener, boolean readOnly) {

        JTextField textField = readOnly ? newReadOnlyTextField(40) : newTextField(40);

        // Ensure it is accessible
        AccessibilityDecorator.apply(textField, MessageKey.EXTENDED_PUBLIC_KEY, MessageKey.EXTENDED_PUBLIC_KEY_TOOLTIP);

        // Detect changes
        textField.getDocument().addDocumentListener(listener);

        // Currently the extended public field does nothing so disable
        textField.setEnabled(false);

        return textField;

    }

    /**
     * @return A new "Password" text field
     */
    public static JPasswordField newPassword() {

        JPasswordField passwordField = new JPasswordField(BitherUI.PASSWORD_LENGTH);

        // Ensure it is accessible
        AccessibilityDecorator.apply(passwordField, MessageKey.ENTER_PASSWORD);

        // Provide a consistent echo character across all components
        passwordField.setEchoChar(getPasswordEchoChar());

        // Limit the length of the underlying document
        DefaultStyledDocument doc = new DefaultStyledDocument();
        doc.setDocumentFilter(new DocumentMaxLengthFilter(BitherUI.PASSWORD_LENGTH));
        passwordField.setDocument(doc);

        // Set the theme
        passwordField.setBorder(new TextBubbleBorder(Themes.currentTheme.dataEntryBorder()));
        passwordField.setBackground(Themes.currentTheme.dataEntryBackground());

        passwordField.setOpaque(false);

        return passwordField;
    }

    /**
     * @param listener The document listener for detecting changes to the content
     * @return A new default public "notes" text area
     */
    public static JTextArea newEnterNotes(DocumentListener listener) {

        JTextArea textArea = TextBoxes.newEnterPrivateNotes(listener, BitherUI.PASSWORD_LENGTH);

        // Ensure it is accessible
        AccessibilityDecorator.apply(textArea, MessageKey.NOTES, MessageKey.NOTES_TOOLTIP);

        return textArea;
    }

    /**
     * @return A new "message" text area (usually for signing for verifying)
     */
    public static JTextArea newEnterMessage() {

        JTextArea textArea = new JTextArea(4, BitherUI.PASSWORD_LENGTH);

        // Ensure it is accessible
        AccessibilityDecorator.apply(textArea, MessageKey.MESSAGE);

        textArea.setOpaque(false);

        // Ensure line wrapping occurs correctly
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        // Ensure TAB transfers focus
        AbstractAction transferFocus = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ((Component) e.getSource()).transferFocus();
            }
        };
        textArea.getInputMap().put(KeyStroke.getKeyStroke("TAB"), "transferFocus");
        textArea.getActionMap().put("transferFocus", transferFocus);

        // Set the theme
        textArea.setBorder(new TextBubbleBorder(Themes.currentTheme.dataEntryBorder()));
        textArea.setBackground(Themes.currentTheme.dataEntryBackground());

        return textArea;
    }

    /**
     * @param listener The document listener for detecting changes to the content
     * @return A new default "private notes" text area
     */
    public static JTextArea newEnterPrivateNotes(DocumentListener listener) {
        return TextBoxes.newEnterPrivateNotes(listener, BitherUI.PASSWORD_LENGTH);
    }

    /**
     * @param listener The document listener for detecting changes to the content
     * @return A new "Notes" text area
     */
    public static JTextArea newEnterPrivateNotes(DocumentListener listener, int width) {

        JTextArea textArea = new JTextArea(6, width);

        // Ensure it is accessible
        AccessibilityDecorator.apply(textArea, MessageKey.PRIVATE_NOTES, MessageKey.PRIVATE_NOTES_TOOLTIP);

        // Limit the length of the underlying document
        DefaultStyledDocument doc = new DefaultStyledDocument();
        doc.setDocumentFilter(new DocumentMaxLengthFilter(BitherUI.SEED_PHRASE_LENGTH));
        textArea.setDocument(doc);

        // Ensure we monitor changes
        doc.addDocumentListener(listener);

        // Ensure line wrapping occurs correctly
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        // Ensure TAB transfers focus
        AbstractAction transferFocus = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ((Component) e.getSource()).transferFocus();
            }
        };
        textArea.getInputMap().put(KeyStroke.getKeyStroke("TAB"), "transferFocus");
        textArea.getActionMap().put("transferFocus", transferFocus);

        // Set the theme
        textArea.setBorder(new TextBubbleBorder(Themes.currentTheme.dataEntryBorder()));
        textArea.setBackground(Themes.currentTheme.dataEntryBackground());

        textArea.setOpaque(false);

        return textArea;
    }

    /**
     * <p>Create a new truncated localised comma separated list label (e.g. "a, b, c ..."</p>
     *
     * @param contents  The contents to join into a localised comma-separated list
     * @param maxLength The maximum length of the resulting string (including ellipsis)
     * @return A new truncated list text area
     */
    public static JTextArea newTruncatedList(Collection<String> contents, int maxLength) {

        JTextArea textArea = new JTextArea(Languages.truncatedList(contents, maxLength));

        textArea.setBorder(BorderFactory.createEmptyBorder());
        textArea.setEditable(false);

        // Ensure the background is transparent
        textArea.setBackground(new Color(0, 0, 0, 0));
        textArea.setForeground(Themes.currentTheme.text());
        textArea.setOpaque(false);

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        return textArea;
    }

    /**
     * @return A new "seed phrase" text area for display only (no copy/paste etc)
     */
    public static JTextArea newDisplaySeedPhrase() {

        // Build off the enter seed phrase
        JTextArea textArea = newEnterSeedPhrase();

        // Ensure it is accessible
        AccessibilityDecorator.apply(textArea, MessageKey.SEED_PHRASE, MessageKey.SEED_PHRASE_TOOLTIP);

        // Prevent copy/paste operations
        textArea.setTransferHandler(null);
        textArea.setEditable(false);

        // Set the theme
        textArea.setBorder(new TextBubbleBorder(Themes.currentTheme.readOnlyBorder()));
        textArea.setBackground(Themes.currentTheme.readOnlyBackground());

        return textArea;

    }

    /**
     * @return A new "seed phrase" text area for entry
     */
    public static JTextArea newEnterSeedPhrase() {

        // Limit the length of the underlying document
        DefaultStyledDocument doc = new DefaultStyledDocument();
        doc.setDocumentFilter(new DocumentMaxLengthFilter(BitherUI.SEED_PHRASE_LENGTH));

        // Keep this in line with the PASSWORD_AREA constant
        JTextArea textArea = new JTextArea(doc, "", 6, BitherUI.PASSWORD_LENGTH);

        // Ensure it is accessible
        AccessibilityDecorator.apply(textArea, MessageKey.SEED_PHRASE);

        // Ensure TAB transfers focus
        AbstractAction transferFocus = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ((Component) e.getSource()).transferFocus();
            }
        };
        textArea.getInputMap().put(KeyStroke.getKeyStroke("TAB"), "transferFocus");
        textArea.getActionMap().put("transferFocus", transferFocus);

        // Ensure line and word wrapping occur as required
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        // Set the theme
        textArea.setBorder(new TextBubbleBorder(Themes.currentTheme.dataEntryBorder()));
        textArea.setBackground(Themes.currentTheme.dataEntryBackground());
        textArea.setFont(new Font("Courier New", Font.PLAIN, 14));

        return textArea;

    }

    /**
     * @return A text area with similar dimensions to a V1 Trezor
     */
    public static JTextArea newTrezorV1Display() {
        return newReadOnlyTextArea(5, 50);
    }

    /**
     * @param listener A document listener to detect changes
     * @return A new "enter API key" text field
     */
    public static JTextField newEnterApiKey(DocumentListener listener) {

        JTextField textField = newTextField(40);

        // Ensure it is accessible
        AccessibilityDecorator.apply(textField, MessageKey.ENTER_ACCESS_CODE, MessageKey.ENTER_ACCESS_CODE_TOOLTIP);

        textField.getDocument().addDocumentListener(listener);

        return textField;

    }

    /**
     * @return The themed echo character for credentials fields
     */
    public static char getPasswordEchoChar() {
        return '\u2022';
    }
}
