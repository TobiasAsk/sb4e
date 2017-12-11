package com.oracle.javafx.scenebuilder.kit.metadata;

import java.util.HashMap;
import java.util.Map;

import com.oracle.javafx.scenebuilder.kit.fxom.FXOMIntrinsic;
import com.oracle.javafx.scenebuilder.kit.metadata.klass.ComponentClassMetadata;

public class JavaFxComponentMetadataProvider implements IComponentClassMetadataProvider {

    // Abstract Component Classes

    private final ComponentClassMetadata NodeMetadata = 
            new ComponentClassMetadata(javafx.scene.Node.class, null);
    private final ComponentClassMetadata ParentMetadata = 
            new ComponentClassMetadata(javafx.scene.Parent.class, NodeMetadata);
    private final ComponentClassMetadata RegionMetadata = 
            new ComponentClassMetadata(javafx.scene.layout.Region.class, ParentMetadata);
    private final ComponentClassMetadata PaneMetadata = 
            new ComponentClassMetadata(javafx.scene.layout.Pane.class, RegionMetadata);
    private final ComponentClassMetadata ControlMetadata = 
            new ComponentClassMetadata(javafx.scene.control.Control.class, RegionMetadata);
    private final ComponentClassMetadata LabeledMetadata = 
            new ComponentClassMetadata(javafx.scene.control.Labeled.class, ControlMetadata);
    private final ComponentClassMetadata ButtonBaseMetadata = 
            new ComponentClassMetadata(javafx.scene.control.ButtonBase.class, LabeledMetadata);
    private final ComponentClassMetadata ComboBoxBaseMetadata = 
            new ComponentClassMetadata(javafx.scene.control.ComboBoxBase.class, ControlMetadata);
    private final ComponentClassMetadata PopupWindowMetadata = 
            new ComponentClassMetadata(javafx.stage.PopupWindow.class, null);
    private final ComponentClassMetadata PopupControlMetadata = 
            new ComponentClassMetadata(javafx.scene.control.PopupControl.class, PopupWindowMetadata);
    private final ComponentClassMetadata TextInputControlMetadata = 
            new ComponentClassMetadata(javafx.scene.control.TextInputControl.class, ControlMetadata);
    private final ComponentClassMetadata TableColumnBaseMetadata = 
            new ComponentClassMetadata(javafx.scene.control.TableColumnBase.class, null);
    private final ComponentClassMetadata MenuItemMetadata = 
            new ComponentClassMetadata(javafx.scene.control.MenuItem.class, null);
    private final ComponentClassMetadata TextFieldMetadata = 
            new ComponentClassMetadata(javafx.scene.control.TextField.class, TextInputControlMetadata);
    private final ComponentClassMetadata ProgressIndicatorMetadata = 
            new ComponentClassMetadata(javafx.scene.control.ProgressIndicator.class, ControlMetadata);
    private final ComponentClassMetadata ToggleButtonMetadata = 
            new ComponentClassMetadata(javafx.scene.control.ToggleButton.class, ButtonBaseMetadata);
    private final ComponentClassMetadata AxisMetadata = 
            new ComponentClassMetadata(javafx.scene.chart.Axis.class, RegionMetadata);
    private final ComponentClassMetadata ChartMetadata = 
            new ComponentClassMetadata(javafx.scene.chart.Chart.class, RegionMetadata);
    private final ComponentClassMetadata ValueAxisMetadata = 
            new ComponentClassMetadata(javafx.scene.chart.ValueAxis.class, AxisMetadata);
    private final ComponentClassMetadata XYChartMetadata = 
            new ComponentClassMetadata(javafx.scene.chart.XYChart.class, ChartMetadata);
    private final ComponentClassMetadata ShapeMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.Shape.class, NodeMetadata);
    private final ComponentClassMetadata PathElementMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.PathElement.class, null);
    private final ComponentClassMetadata CameraMetadata = 
            new ComponentClassMetadata(javafx.scene.Camera.class, NodeMetadata);
    private final ComponentClassMetadata LightBaseMetadata = 
            new ComponentClassMetadata(javafx.scene.LightBase.class, NodeMetadata);
    private final ComponentClassMetadata Shape3DMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.Shape3D.class, NodeMetadata);

    // Other Component Classes (in alphabetical order)

    private final ComponentClassMetadata SwingNodeMetadata = 
            new ComponentClassMetadata(javafx.embed.swing.SwingNode.class, NodeMetadata);
    private final ComponentClassMetadata AmbientLightMetadata = 
            new ComponentClassMetadata(javafx.scene.AmbientLight.class, LightBaseMetadata);
    private final ComponentClassMetadata GroupMetadata =
            new ComponentClassMetadata(javafx.scene.Group.class, ParentMetadata);
    private final ComponentClassMetadata ParallelCameraMetadata = 
            new ComponentClassMetadata(javafx.scene.ParallelCamera.class, CameraMetadata);
    private final ComponentClassMetadata PerspectiveCameraMetadata = 
            new ComponentClassMetadata(javafx.scene.PerspectiveCamera.class, CameraMetadata);
    private final ComponentClassMetadata PointLightMetadata = 
            new ComponentClassMetadata(javafx.scene.PointLight.class, LightBaseMetadata);
    private final ComponentClassMetadata SubSceneMetadata = 
            new ComponentClassMetadata(javafx.scene.SubScene.class, NodeMetadata);
    private final ComponentClassMetadata CanvasMetadata = 
            new ComponentClassMetadata(javafx.scene.canvas.Canvas.class, NodeMetadata);
    private final ComponentClassMetadata AreaChartMetadata = 
            new ComponentClassMetadata(javafx.scene.chart.AreaChart.class, XYChartMetadata);
    private final ComponentClassMetadata BarChartMetadata = 
            new ComponentClassMetadata(javafx.scene.chart.BarChart.class, XYChartMetadata);
    private final ComponentClassMetadata BubbleChartMetadata =
            new ComponentClassMetadata(javafx.scene.chart.BubbleChart.class, XYChartMetadata);
    private final ComponentClassMetadata CategoryAxisMetadata = 
            new ComponentClassMetadata(javafx.scene.chart.CategoryAxis.class, AxisMetadata);
    private final ComponentClassMetadata LineChartMetadata =
            new ComponentClassMetadata(javafx.scene.chart.LineChart.class, XYChartMetadata);
    private final ComponentClassMetadata NumberAxisMetadata = 
            new ComponentClassMetadata(javafx.scene.chart.NumberAxis.class, ValueAxisMetadata);
    private final ComponentClassMetadata PieChartMetadata = 
            new ComponentClassMetadata(javafx.scene.chart.PieChart.class, ChartMetadata);
    private final ComponentClassMetadata ScatterChartMetadata = 
            new ComponentClassMetadata(javafx.scene.chart.ScatterChart.class, XYChartMetadata);
    private final ComponentClassMetadata StackedAreaChartMetadata = 
            new ComponentClassMetadata(javafx.scene.chart.StackedAreaChart.class, XYChartMetadata);
    private final ComponentClassMetadata StackedBarChartMetadata = 
            new ComponentClassMetadata(javafx.scene.chart.StackedBarChart.class, XYChartMetadata);
    private final ComponentClassMetadata AccordionMetadata =
            new ComponentClassMetadata(javafx.scene.control.Accordion.class, ControlMetadata);
    private final ComponentClassMetadata ButtonMetadata = 
            new ComponentClassMetadata(javafx.scene.control.Button.class, ButtonBaseMetadata);
    private final ComponentClassMetadata ButtonBarMetadata = 
            new ComponentClassMetadata(javafx.scene.control.ButtonBar.class, ControlMetadata);
    private final ComponentClassMetadata CheckBoxMetadata = 
            new ComponentClassMetadata(javafx.scene.control.CheckBox.class, ButtonBaseMetadata);
    private final ComponentClassMetadata CheckMenuItemMetadata = 
            new ComponentClassMetadata(javafx.scene.control.CheckMenuItem.class, MenuItemMetadata);
    private final ComponentClassMetadata ChoiceBoxMetadata = 
            new ComponentClassMetadata(javafx.scene.control.ChoiceBox.class, ControlMetadata);
    private final ComponentClassMetadata ColorPickerMetadata =
            new ComponentClassMetadata(javafx.scene.control.ColorPicker.class, ComboBoxBaseMetadata);
    private final ComponentClassMetadata ComboBoxMetadata = 
            new ComponentClassMetadata(javafx.scene.control.ComboBox.class, ComboBoxBaseMetadata);
    private final ComponentClassMetadata ContextMenuMetadata = 
            new ComponentClassMetadata(javafx.scene.control.ContextMenu.class, PopupControlMetadata);
    private final ComponentClassMetadata CustomMenuItemMetadata = 
            new ComponentClassMetadata(javafx.scene.control.CustomMenuItem.class, MenuItemMetadata);
    private final ComponentClassMetadata DatePickerMetadata = 
            new ComponentClassMetadata(javafx.scene.control.DatePicker.class, ComboBoxBaseMetadata);
    private final ComponentClassMetadata DialogPaneMetadata = 
            new ComponentClassMetadata(javafx.scene.control.DialogPane.class, PaneMetadata);
    private final ComponentClassMetadata HyperlinkMetadata = 
            new ComponentClassMetadata(javafx.scene.control.Hyperlink.class, ButtonBaseMetadata);
    private final ComponentClassMetadata LabelMetadata = 
            new ComponentClassMetadata(javafx.scene.control.Label.class, LabeledMetadata);
    private final ComponentClassMetadata ListViewMetadata = 
            new ComponentClassMetadata(javafx.scene.control.ListView.class, ControlMetadata);
    private final ComponentClassMetadata MenuMetadata = 
            new ComponentClassMetadata(javafx.scene.control.Menu.class, MenuItemMetadata);
    private final ComponentClassMetadata MenuBarMetadata = 
            new ComponentClassMetadata(javafx.scene.control.MenuBar.class, ControlMetadata);
    private final ComponentClassMetadata MenuButtonMetadata = 
            new ComponentClassMetadata(javafx.scene.control.MenuButton.class, ButtonBaseMetadata);
    private final ComponentClassMetadata PaginationMetadata = 
            new ComponentClassMetadata(javafx.scene.control.Pagination.class, ControlMetadata);
    private final ComponentClassMetadata PasswordFieldMetadata = 
            new ComponentClassMetadata(javafx.scene.control.PasswordField.class, TextFieldMetadata);
    private final ComponentClassMetadata ProgressBarMetadata = 
            new ComponentClassMetadata(javafx.scene.control.ProgressBar.class, ProgressIndicatorMetadata);
    private final ComponentClassMetadata RadioButtonMetadata = 
            new ComponentClassMetadata(javafx.scene.control.RadioButton.class, ToggleButtonMetadata);
    private final ComponentClassMetadata RadioMenuItemMetadata = 
            new ComponentClassMetadata(javafx.scene.control.RadioMenuItem.class, MenuItemMetadata);
    private final ComponentClassMetadata ScrollBarMetadata = 
            new ComponentClassMetadata(javafx.scene.control.ScrollBar.class, ControlMetadata);
    private final ComponentClassMetadata ScrollPaneMetadata = 
            new ComponentClassMetadata(javafx.scene.control.ScrollPane.class, ControlMetadata);
    private final ComponentClassMetadata SeparatorMetadata = 
            new ComponentClassMetadata(javafx.scene.control.Separator.class, ControlMetadata);
    private final ComponentClassMetadata SeparatorMenuItemMetadata = 
            new ComponentClassMetadata(javafx.scene.control.SeparatorMenuItem.class, CustomMenuItemMetadata);
    private final ComponentClassMetadata SliderMetadata = 
            new ComponentClassMetadata(javafx.scene.control.Slider.class, ControlMetadata);
    private final ComponentClassMetadata SpinnerMetadata = 
            new ComponentClassMetadata(javafx.scene.control.Spinner.class, ControlMetadata);
    private final ComponentClassMetadata SplitMenuButtonMetadata = 
            new ComponentClassMetadata(javafx.scene.control.SplitMenuButton.class, MenuButtonMetadata);
    private final ComponentClassMetadata SplitPaneMetadata = 
            new ComponentClassMetadata(javafx.scene.control.SplitPane.class, ControlMetadata);
    private final ComponentClassMetadata TabMetadata = 
            new ComponentClassMetadata(javafx.scene.control.Tab.class, null);
    private final ComponentClassMetadata TabPaneMetadata = 
            new ComponentClassMetadata(javafx.scene.control.TabPane.class, ControlMetadata);
    private final ComponentClassMetadata TableColumnMetadata = 
            new ComponentClassMetadata(javafx.scene.control.TableColumn.class, TableColumnBaseMetadata);
    private final ComponentClassMetadata TableViewMetadata = 
            new ComponentClassMetadata(javafx.scene.control.TableView.class, ControlMetadata);
    private final ComponentClassMetadata TextAreaMetadata = 
            new ComponentClassMetadata(javafx.scene.control.TextArea.class, TextInputControlMetadata);
    private final ComponentClassMetadata TextFormatterMetadata = 
            new ComponentClassMetadata(javafx.scene.control.TextFormatter.class, null);
    private final ComponentClassMetadata TitledPaneMetadata = 
            new ComponentClassMetadata(javafx.scene.control.TitledPane.class, LabeledMetadata);
    private final ComponentClassMetadata ToolBarMetadata = 
            new ComponentClassMetadata(javafx.scene.control.ToolBar.class, ControlMetadata);
    private final ComponentClassMetadata TooltipMetadata = 
            new ComponentClassMetadata(javafx.scene.control.Tooltip.class, PopupControlMetadata);
    private final ComponentClassMetadata TreeTableColumnMetadata = 
            new ComponentClassMetadata(javafx.scene.control.TreeTableColumn.class, TableColumnBaseMetadata);
    private final ComponentClassMetadata TreeTableViewMetadata = 
            new ComponentClassMetadata(javafx.scene.control.TreeTableView.class, ControlMetadata);
    private final ComponentClassMetadata TreeViewMetadata = 
            new ComponentClassMetadata(javafx.scene.control.TreeView.class, ControlMetadata);
    private final ComponentClassMetadata ImageViewMetadata = 
            new ComponentClassMetadata(javafx.scene.image.ImageView.class, NodeMetadata);
    private final ComponentClassMetadata AnchorPaneMetadata = 
            new ComponentClassMetadata(javafx.scene.layout.AnchorPane.class, PaneMetadata);
    private final ComponentClassMetadata BorderPaneMetadata = 
            new ComponentClassMetadata(javafx.scene.layout.BorderPane.class, PaneMetadata);
    private final ComponentClassMetadata ColumnConstraintsMetadata = 
            new ComponentClassMetadata(javafx.scene.layout.ColumnConstraints.class, null);
    private final ComponentClassMetadata FlowPaneMetadata = 
            new ComponentClassMetadata(javafx.scene.layout.FlowPane.class, PaneMetadata);
    private final ComponentClassMetadata GridPaneMetadata = 
            new ComponentClassMetadata(javafx.scene.layout.GridPane.class, PaneMetadata);
    private final ComponentClassMetadata HBoxMetadata = 
            new ComponentClassMetadata(javafx.scene.layout.HBox.class, PaneMetadata);
    private final ComponentClassMetadata RowConstraintsMetadata = 
            new ComponentClassMetadata(javafx.scene.layout.RowConstraints.class, null);
    private final ComponentClassMetadata StackPaneMetadata = 
            new ComponentClassMetadata(javafx.scene.layout.StackPane.class, PaneMetadata);
    private final ComponentClassMetadata TilePaneMetadata = 
            new ComponentClassMetadata(javafx.scene.layout.TilePane.class, PaneMetadata);
    private final ComponentClassMetadata VBoxMetadata = 
            new ComponentClassMetadata(javafx.scene.layout.VBox.class, PaneMetadata);
    private final ComponentClassMetadata MediaViewMetadata = 
            new ComponentClassMetadata(javafx.scene.media.MediaView.class, NodeMetadata);
    private final ComponentClassMetadata ArcMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.Arc.class, ShapeMetadata);
    private final ComponentClassMetadata ArcToMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.ArcTo.class, PathElementMetadata);
    private final ComponentClassMetadata BoxMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.Box.class, Shape3DMetadata);
    private final ComponentClassMetadata CircleMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.Circle.class, ShapeMetadata);
    private final ComponentClassMetadata ClosePathMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.ClosePath.class, PathElementMetadata);
    private final ComponentClassMetadata CubicCurveMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.CubicCurve.class, ShapeMetadata);
    private final ComponentClassMetadata CubicCurveToMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.CubicCurveTo.class, PathElementMetadata);
    private final ComponentClassMetadata CylinderMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.Cylinder.class, Shape3DMetadata);
    private final ComponentClassMetadata EllipseMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.Ellipse.class, ShapeMetadata);
    private final ComponentClassMetadata HLineToMetadata =
            new ComponentClassMetadata(javafx.scene.shape.HLineTo.class, PathElementMetadata);
    private final ComponentClassMetadata LineMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.Line.class, ShapeMetadata);
    private final ComponentClassMetadata LineToMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.LineTo.class, PathElementMetadata);
    private final ComponentClassMetadata MeshViewMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.MeshView.class, Shape3DMetadata);
    private final ComponentClassMetadata MoveToMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.MoveTo.class, PathElementMetadata);
    private final ComponentClassMetadata PathMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.Path.class, ShapeMetadata);
    private final ComponentClassMetadata PolygonMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.Polygon.class, ShapeMetadata);
    private final ComponentClassMetadata PolylineMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.Polyline.class, ShapeMetadata);
    private final ComponentClassMetadata QuadCurveMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.QuadCurve.class, ShapeMetadata);
    private final ComponentClassMetadata QuadCurveToMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.QuadCurveTo.class, PathElementMetadata);
    private final ComponentClassMetadata RectangleMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.Rectangle.class, ShapeMetadata);
    private final ComponentClassMetadata SVGPathMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.SVGPath.class, ShapeMetadata);
    private final ComponentClassMetadata SphereMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.Sphere.class, Shape3DMetadata);
    private final ComponentClassMetadata VLineToMetadata = 
            new ComponentClassMetadata(javafx.scene.shape.VLineTo.class, PathElementMetadata);
    private final ComponentClassMetadata TextMetadata = 
            new ComponentClassMetadata(javafx.scene.text.Text.class, ShapeMetadata);
    private final ComponentClassMetadata TextFlowMetadata = 
            new ComponentClassMetadata(javafx.scene.text.TextFlow.class, PaneMetadata);
    private final ComponentClassMetadata HTMLEditorMetadata = 
            new ComponentClassMetadata(javafx.scene.web.HTMLEditor.class, ControlMetadata);
    private final ComponentClassMetadata WebViewMetadata = 
            new ComponentClassMetadata(javafx.scene.web.WebView.class, ParentMetadata);
    private final ComponentClassMetadata IncludeElementMetadata =
            new ComponentClassMetadata(FXOMIntrinsic.class, null);

	@Override
	public Map<Class<?>, ComponentClassMetadata> getMetadata() {
		Map<Class<?>, ComponentClassMetadata> componentClassMap = new HashMap<>();
        
		// Populate componentClassMap
        componentClassMap.put(AccordionMetadata.getKlass(), AccordionMetadata);
        componentClassMap.put(AmbientLightMetadata.getKlass(), AmbientLightMetadata);
        componentClassMap.put(AnchorPaneMetadata.getKlass(), AnchorPaneMetadata);
        componentClassMap.put(ArcMetadata.getKlass(), ArcMetadata);
        componentClassMap.put(ArcToMetadata.getKlass(), ArcToMetadata);
        componentClassMap.put(AreaChartMetadata.getKlass(), AreaChartMetadata);
        componentClassMap.put(AxisMetadata.getKlass(), AxisMetadata);
        componentClassMap.put(BarChartMetadata.getKlass(), BarChartMetadata);
        componentClassMap.put(BorderPaneMetadata.getKlass(), BorderPaneMetadata);
        componentClassMap.put(BoxMetadata.getKlass(), BoxMetadata);
        componentClassMap.put(BubbleChartMetadata.getKlass(), BubbleChartMetadata);
        componentClassMap.put(ButtonMetadata.getKlass(), ButtonMetadata);
        componentClassMap.put(ButtonBarMetadata.getKlass(), ButtonBarMetadata);
        componentClassMap.put(ButtonBaseMetadata.getKlass(), ButtonBaseMetadata);
        componentClassMap.put(CameraMetadata.getKlass(), CameraMetadata);
        componentClassMap.put(CanvasMetadata.getKlass(), CanvasMetadata);
        componentClassMap.put(CategoryAxisMetadata.getKlass(), CategoryAxisMetadata);
        componentClassMap.put(ChartMetadata.getKlass(), ChartMetadata);
        componentClassMap.put(CheckBoxMetadata.getKlass(), CheckBoxMetadata);
        componentClassMap.put(CheckMenuItemMetadata.getKlass(), CheckMenuItemMetadata);
        componentClassMap.put(ChoiceBoxMetadata.getKlass(), ChoiceBoxMetadata);
        componentClassMap.put(CircleMetadata.getKlass(), CircleMetadata);
        componentClassMap.put(ClosePathMetadata.getKlass(), ClosePathMetadata);
        componentClassMap.put(ColorPickerMetadata.getKlass(), ColorPickerMetadata);
        componentClassMap.put(ColumnConstraintsMetadata.getKlass(), ColumnConstraintsMetadata);
        componentClassMap.put(ComboBoxMetadata.getKlass(), ComboBoxMetadata);
        componentClassMap.put(ComboBoxBaseMetadata.getKlass(), ComboBoxBaseMetadata);
        componentClassMap.put(ContextMenuMetadata.getKlass(), ContextMenuMetadata);
        componentClassMap.put(ControlMetadata.getKlass(), ControlMetadata);
        componentClassMap.put(CubicCurveMetadata.getKlass(), CubicCurveMetadata);
        componentClassMap.put(CubicCurveToMetadata.getKlass(), CubicCurveToMetadata);
        componentClassMap.put(CustomMenuItemMetadata.getKlass(), CustomMenuItemMetadata);
        componentClassMap.put(CylinderMetadata.getKlass(), CylinderMetadata);
        componentClassMap.put(DatePickerMetadata.getKlass(), DatePickerMetadata);
        componentClassMap.put(DialogPaneMetadata.getKlass(), DialogPaneMetadata);
        componentClassMap.put(EllipseMetadata.getKlass(), EllipseMetadata);
        componentClassMap.put(FlowPaneMetadata.getKlass(), FlowPaneMetadata);
        componentClassMap.put(GridPaneMetadata.getKlass(), GridPaneMetadata);
        componentClassMap.put(GroupMetadata.getKlass(), GroupMetadata);
        componentClassMap.put(HBoxMetadata.getKlass(), HBoxMetadata);
        componentClassMap.put(HLineToMetadata.getKlass(), HLineToMetadata);
        componentClassMap.put(HTMLEditorMetadata.getKlass(), HTMLEditorMetadata);
        componentClassMap.put(HyperlinkMetadata.getKlass(), HyperlinkMetadata);
        componentClassMap.put(ImageViewMetadata.getKlass(), ImageViewMetadata);
        componentClassMap.put(LabelMetadata.getKlass(), LabelMetadata);
        componentClassMap.put(LabeledMetadata.getKlass(), LabeledMetadata);
        componentClassMap.put(LightBaseMetadata.getKlass(), LightBaseMetadata);
        componentClassMap.put(LineMetadata.getKlass(), LineMetadata);
        componentClassMap.put(LineChartMetadata.getKlass(), LineChartMetadata);
        componentClassMap.put(LineToMetadata.getKlass(), LineToMetadata);
        componentClassMap.put(ListViewMetadata.getKlass(), ListViewMetadata);
        componentClassMap.put(MediaViewMetadata.getKlass(), MediaViewMetadata);
        componentClassMap.put(MenuMetadata.getKlass(), MenuMetadata);
        componentClassMap.put(MenuBarMetadata.getKlass(), MenuBarMetadata);
        componentClassMap.put(MenuButtonMetadata.getKlass(), MenuButtonMetadata);
        componentClassMap.put(MenuItemMetadata.getKlass(), MenuItemMetadata);
        componentClassMap.put(MeshViewMetadata.getKlass(), MeshViewMetadata);
        componentClassMap.put(MoveToMetadata.getKlass(), MoveToMetadata);
        componentClassMap.put(NodeMetadata.getKlass(), NodeMetadata);
        componentClassMap.put(NumberAxisMetadata.getKlass(), NumberAxisMetadata);
        componentClassMap.put(PaginationMetadata.getKlass(), PaginationMetadata);
        componentClassMap.put(PaneMetadata.getKlass(), PaneMetadata);
        componentClassMap.put(ParallelCameraMetadata.getKlass(), ParallelCameraMetadata);
        componentClassMap.put(ParentMetadata.getKlass(), ParentMetadata);
        componentClassMap.put(PasswordFieldMetadata.getKlass(), PasswordFieldMetadata);
        componentClassMap.put(PathMetadata.getKlass(), PathMetadata);
        componentClassMap.put(PathElementMetadata.getKlass(), PathElementMetadata);
        componentClassMap.put(PerspectiveCameraMetadata.getKlass(), PerspectiveCameraMetadata);
        componentClassMap.put(PieChartMetadata.getKlass(), PieChartMetadata);
        componentClassMap.put(PointLightMetadata.getKlass(), PointLightMetadata);
        componentClassMap.put(PolygonMetadata.getKlass(), PolygonMetadata);
        componentClassMap.put(PolylineMetadata.getKlass(), PolylineMetadata);
        componentClassMap.put(PopupControlMetadata.getKlass(), PopupControlMetadata);
        componentClassMap.put(PopupWindowMetadata.getKlass(), PopupWindowMetadata);
        componentClassMap.put(ProgressBarMetadata.getKlass(), ProgressBarMetadata);
        componentClassMap.put(ProgressIndicatorMetadata.getKlass(), ProgressIndicatorMetadata);
        componentClassMap.put(QuadCurveMetadata.getKlass(), QuadCurveMetadata);
        componentClassMap.put(QuadCurveToMetadata.getKlass(), QuadCurveToMetadata);
        componentClassMap.put(RadioButtonMetadata.getKlass(), RadioButtonMetadata);
        componentClassMap.put(RadioMenuItemMetadata.getKlass(), RadioMenuItemMetadata);
        componentClassMap.put(RectangleMetadata.getKlass(), RectangleMetadata);
        componentClassMap.put(RegionMetadata.getKlass(), RegionMetadata);
        componentClassMap.put(RowConstraintsMetadata.getKlass(), RowConstraintsMetadata);
        componentClassMap.put(SVGPathMetadata.getKlass(), SVGPathMetadata);
        componentClassMap.put(ScatterChartMetadata.getKlass(), ScatterChartMetadata);
        componentClassMap.put(ScrollBarMetadata.getKlass(), ScrollBarMetadata);
        componentClassMap.put(ScrollPaneMetadata.getKlass(), ScrollPaneMetadata);
        componentClassMap.put(SeparatorMetadata.getKlass(), SeparatorMetadata);
        componentClassMap.put(SeparatorMenuItemMetadata.getKlass(), SeparatorMenuItemMetadata);
        componentClassMap.put(ShapeMetadata.getKlass(), ShapeMetadata);
        componentClassMap.put(Shape3DMetadata.getKlass(), Shape3DMetadata);
        componentClassMap.put(SliderMetadata.getKlass(), SliderMetadata);
        componentClassMap.put(SphereMetadata.getKlass(), SphereMetadata);
        componentClassMap.put(SpinnerMetadata.getKlass(), SpinnerMetadata);
        componentClassMap.put(SplitMenuButtonMetadata.getKlass(), SplitMenuButtonMetadata);
        componentClassMap.put(SplitPaneMetadata.getKlass(), SplitPaneMetadata);
        componentClassMap.put(StackPaneMetadata.getKlass(), StackPaneMetadata);
        componentClassMap.put(StackedAreaChartMetadata.getKlass(), StackedAreaChartMetadata);
        componentClassMap.put(StackedBarChartMetadata.getKlass(), StackedBarChartMetadata);
        componentClassMap.put(SubSceneMetadata.getKlass(), SubSceneMetadata);
        componentClassMap.put(SwingNodeMetadata.getKlass(), SwingNodeMetadata);
        componentClassMap.put(TabMetadata.getKlass(), TabMetadata);
        componentClassMap.put(TabPaneMetadata.getKlass(), TabPaneMetadata);
        componentClassMap.put(TableColumnMetadata.getKlass(), TableColumnMetadata);
        componentClassMap.put(TableColumnBaseMetadata.getKlass(), TableColumnBaseMetadata);
        componentClassMap.put(TableViewMetadata.getKlass(), TableViewMetadata);
        componentClassMap.put(TextMetadata.getKlass(), TextMetadata);
        componentClassMap.put(TextAreaMetadata.getKlass(), TextAreaMetadata);
        componentClassMap.put(TextFieldMetadata.getKlass(), TextFieldMetadata);
        componentClassMap.put(TextFlowMetadata.getKlass(), TextFlowMetadata);
        componentClassMap.put(TextFormatterMetadata.getKlass(), TextFormatterMetadata);
        componentClassMap.put(TextInputControlMetadata.getKlass(), TextInputControlMetadata);
        componentClassMap.put(TilePaneMetadata.getKlass(), TilePaneMetadata);
        componentClassMap.put(TitledPaneMetadata.getKlass(), TitledPaneMetadata);
        componentClassMap.put(ToggleButtonMetadata.getKlass(), ToggleButtonMetadata);
        componentClassMap.put(ToolBarMetadata.getKlass(), ToolBarMetadata);
        componentClassMap.put(TooltipMetadata.getKlass(), TooltipMetadata);
        componentClassMap.put(TreeTableColumnMetadata.getKlass(), TreeTableColumnMetadata);
        componentClassMap.put(TreeTableViewMetadata.getKlass(), TreeTableViewMetadata);
        componentClassMap.put(TreeViewMetadata.getKlass(), TreeViewMetadata);
        componentClassMap.put(VBoxMetadata.getKlass(), VBoxMetadata);
        componentClassMap.put(VLineToMetadata.getKlass(), VLineToMetadata);
        componentClassMap.put(ValueAxisMetadata.getKlass(), ValueAxisMetadata);
        componentClassMap.put(WebViewMetadata.getKlass(), WebViewMetadata);
        componentClassMap.put(XYChartMetadata.getKlass(), XYChartMetadata);
        componentClassMap.put(IncludeElementMetadata.getKlass(), IncludeElementMetadata);
		return componentClassMap;
	}

}
