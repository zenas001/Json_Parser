package com.godwin.jsonparser.ui.forms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.weisj.jsvg.S;
import com.godwin.jsonparser.common.Logger;
import com.godwin.jsonparser.ui.IParserWidget;
import com.godwin.jsonparser.ui.TreeNodeCreator;
import com.godwin.jsonparser.ui.action.CopyToClipBoardAction;
import com.godwin.jsonparser.ui.action.JBRadioAction;
import com.godwin.jsonparser.util.EditorHintsNotifier;
import com.godwin.jsonparser.util.JsonUtils;
import com.google.gson.JsonSyntaxException;
import com.intellij.icons.AllIcons;
import com.intellij.ide.highlighter.HtmlFileHighlighter;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.ide.highlighter.XmlFileHighlighter;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.json.JsonFileType;
import com.intellij.json.highlighting.JsonSyntaxHighlighterFactory;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.util.LayerDescriptor;
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.apache.http.util.TextUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Godwin on 8/12/2018 12:49 PM for json.
 *
 * @author : Godwin Joseph Kurinjikattu
 */
public class ParserBodyWidget {
    public JPanel container;
    private JPanel previewTypeContainer;
    private JPanel prettyContainer;
    private JPanel rawContainer;
    private JPanel minimalContainer;   //TODO add Minimal
    private JTree outputTree;

    private JScrollPane treeContainer;
    private JPanel toolBarContainer;

    private ButtonGroup buttonGroup;
    private CardLayout mPreviewTypeCardLayout;
    private SimpleToolWindowPanel simpleToolWindowPanel1;

    private final Editor prettyEditor;
    private final Editor rawEditor;
    //TODO add Minimal
    private final Editor minimalEditor;

    private static final IElementType TextElementType = new IElementType("TEXT", Language.ANY);

    private final Project mProject;

    private final ActionListener previewTypeListener = e -> mPreviewTypeCardLayout.show(previewTypeContainer, e.getActionCommand());
    private final IParserWidget parserWidget;

    private static final String PRETTY = "Pretty";
    private static final String MINIMAL = "Minimal";

    private static final String RAW = "Raw";
    private static final String TREE = "Tree";

    public ParserBodyWidget(Project mProject, IParserWidget parserWidget) {

        this.mProject = mProject;
        this.parserWidget = parserWidget;

        mPreviewTypeCardLayout = ((CardLayout) previewTypeContainer.getLayout());

        prettyEditor = createEditor();
        prettyContainer.add(prettyEditor.getComponent(), BorderLayout.CENTER);
        rawEditor = createEditor();
        rawContainer.add(rawEditor.getComponent(), BorderLayout.CENTER);
        //TODO add Minimal
        minimalEditor = createEditor();
        //TODO add Minimal
        minimalContainer.add(minimalEditor.getComponent(), BorderLayout.CENTER);

        changeIcon();
        setEmptyTree();

        setUiComponents();
    }

    private void setUiComponents() {
        simpleToolWindowPanel1 = new SimpleToolWindowPanel(true, true);
        buttonGroup = new ButtonGroup();
        ActionGroup group = new DefaultActionGroup(
                new JBRadioAction(PRETTY, PRETTY, buttonGroup, previewTypeListener, true),
                //TODO add Minimal
                new JBRadioAction(MINIMAL, MINIMAL, buttonGroup, previewTypeListener),
                new JBRadioAction(RAW, RAW, buttonGroup, previewTypeListener),
                new JBRadioAction(TREE, TREE, buttonGroup, previewTypeListener),
                new CopyToClipBoardAction("Copy to Clipboard", "Click to copy selected text to clipboard", AllIcons.Actions.Copy),
                new AnAction("Use Soft Wraps", "Toggle using soft wraps in current editor", AllIcons.Actions.ToggleSoftWrap) {
                    @Override
                    public void actionPerformed(AnActionEvent anActionEvent) {
                        try {
                            String actionCommand = buttonGroup.getSelection().getActionCommand();
                            if (PRETTY.equalsIgnoreCase(actionCommand)) {
                                EditorSettings settings = prettyEditor.getSettings();
                                settings.setUseSoftWraps(!settings.isUseSoftWraps());
                            } else if (RAW.equalsIgnoreCase(actionCommand)) {
                                EditorSettings settings = rawEditor.getSettings();
                                settings.setUseSoftWraps(!settings.isUseSoftWraps());
                            } else if (MINIMAL.equalsIgnoreCase(actionCommand)) {
                                EditorSettings settings = minimalEditor.getSettings();
                                settings.setUseSoftWraps(!settings.isUseSoftWraps());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
//                new NewWindowAction(parserWidget)
        );

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, group, true);

        simpleToolWindowPanel1.setToolbar(toolbar.getComponent());
        simpleToolWindowPanel1.setContent(new JPanel(new BorderLayout()));
    }

    private void createUIComponents() {
        setUiComponents();
    }

    private Editor createEditor() {
        PsiFile myFile = null;
        EditorFactory editorFactory = EditorFactory.getInstance();
        Document doc = myFile == null
                ? editorFactory.createDocument("")
                : PsiDocumentManager.getInstance(mProject).getDocument(myFile);
        Editor editor = editorFactory.createEditor(doc, mProject);
        EditorSettings editorSettings = editor.getSettings();
        editorSettings.setVirtualSpace(false);
        editorSettings.setLineMarkerAreaShown(false);
        editorSettings.setIndentGuidesShown(false);
        editorSettings.setFoldingOutlineShown(true);
        editorSettings.setAdditionalColumnsCount(3);
        editorSettings.setAdditionalLinesCount(3);
        editorSettings.setLineNumbersShown(true);
        editorSettings.setCaretRowShown(true);

        ((EditorEx) editor).setHighlighter(createHighlighter(FileTypes.PLAIN_TEXT));
        return editor;
    }

    private EditorHighlighter createHighlighter(LanguageFileType fileType) {

        SyntaxHighlighter originalHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(fileType, null, null);
        if (originalHighlighter == null) {
            originalHighlighter = new PlainSyntaxHighlighter();
        }

        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
        LayeredLexerEditorHighlighter highlighter = new LayeredLexerEditorHighlighter(getFileHighlighter(fileType), scheme);
        highlighter.registerLayer(TextElementType, new LayerDescriptor(originalHighlighter, ""));
        return highlighter;
    }

    private SyntaxHighlighter getFileHighlighter(FileType fileType) {
        if (fileType == HtmlFileType.INSTANCE) {
            return new HtmlFileHighlighter();
        } else if (fileType == XmlFileType.INSTANCE) {
            return new XmlFileHighlighter();
        } else if (fileType == JsonFileType.INSTANCE) {
            return JsonSyntaxHighlighterFactory.getSyntaxHighlighter(fileType, mProject, null);
        }
        return new PlainSyntaxHighlighter();
    }

    private LanguageFileType getFileType(/*Header[] contentTypes*/) {
//        if (contentTypes != null && contentTypes.length > 0) {
//            Header contentType = contentTypes[0];
//            if (contentType.getValue().contains("text/html")) {
//                return HtmlFileType.INSTANCE;
//            } else if (contentType.getValue().contains("application/xml")) {
//                return XmlFileType.INSTANCE;
//            } else if (contentType.getValue().contains("application/json")) {
//                return JsonFileType.INSTANCE;
//            }
//        }
//        return PlainTextFileType.INSTANCE;
        return JsonFileType.INSTANCE;
    }

    public void showPretty(String text) {

        try {
            String prettyJsonString;
            if (TextUtils.isEmpty(text)) {
                prettyJsonString = "";
            } else {
                prettyJsonString = JsonUtils.formatJson(text);
            }

            writeToEditor(prettyJsonString, prettyEditor);

        } catch (Exception e) {
            doErr(text, e, prettyEditor);
        }
    }

    //TODO add Minimal
    public void showMinimal(String minimalJson) {
        try {
            String minimalJsonString;
            if (TextUtils.isEmpty(minimalJson)) {
                minimalJsonString = "";
            } else {
                minimalJsonString = JsonUtils.minifyJson(minimalJson);
            }
            writeToEditor(minimalJsonString, minimalEditor);
        } catch (Exception e) {
            doErr(minimalJson, e, minimalEditor);
        }
    }

    private void writeToEditor(String prettyJsonString, Editor editor) {
        WriteCommandAction.runWriteCommandAction(mProject, () -> {
            Document document = editor.getDocument();
            document.setReadOnly(false);
            document.setText(prettyJsonString);
            document.setReadOnly(true);
        });
        LanguageFileType fileType = getFileType();
        ((EditorEx) editor).setHighlighter(createHighlighter(fileType));
    }

    private void doErr(String text, Exception e, Editor editor) {
        //            e.printStackTrace();
        if (e instanceof JsonSyntaxException) {
            String message = e.getMessage();
            if (TextUtils.isEmpty(message) && e.getCause() != null && !TextUtils.isEmpty(e.getCause().getMessage())) {
                message = e.getCause().getMessage();
            }
            String finalMessage = message;
            writeToEditor(text + "\n\n\n" + finalMessage, editor);
        } else if (e instanceof JsonProcessingException) {

            writeToEditor(text, editor);

            JsonProcessingException exception = (JsonProcessingException) e;
            String originalMessage = exception.getOriginalMessage();
            long charOffset = exception.getLocation().getCharOffset();


//
            EditorHintsNotifier.notifyError(Objects.requireNonNull(editor), originalMessage, charOffset);

        }
    }


    public void showRaw(String text) {
        if (null == text)
            return;
        try {
            WriteCommandAction.runWriteCommandAction(mProject, () -> rawEditor.getDocument().setText(text));
        } catch (Exception e) {
//            e.printStackTrace();
            Logger.e("json Error catch");
        }
    }


    public void showTree(String jsonString) {
        if (TextUtils.isEmpty(jsonString)) {
            setEmptyTree();
            return;
        }
        try {
            Map<String, Object> prettyJsonString = JsonUtils.getMap(jsonString);

            DefaultTreeModel model = TreeNodeCreator.getTreeModelFromMap(prettyJsonString);
            outputTree.setModel(model);
            expandAllNodes(outputTree, 0, outputTree.getRowCount());
        } catch (Exception e) {
//            e.printStackTrace();
            Logger.e("json Error catch");
        }
    }

    private void changeIcon() {
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) outputTree.getCellRenderer();
        Icon icon = new ImageIcon();
        renderer.setClosedIcon(AllIcons.General.ArrowRight);
        renderer.setOpenIcon(AllIcons.General.ArrowDown);
        renderer.setLeafIcon(AllIcons.Nodes.C_plocal);
    }

    private void setEmptyTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("");

        DefaultTreeModel model = new DefaultTreeModel(root);
        outputTree.setModel(model);

    }

    private void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
        for (int i = startingIndex; i < rowCount; ++i) {
            tree.expandRow(i);
        }

        if (tree.getRowCount() != rowCount) {
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }
}
