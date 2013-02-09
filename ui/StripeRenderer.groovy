import java.awt.*;
import javax.swing.*;

class StripeRenderer extends DefaultListCellRenderer {

    MainWindow mainWindow

    public StripeRenderer(MainWindow mainWindow) {
        this.mainWindow = mainWindow
    }

    public Component getListCellRendererComponent(JList list, Object pkg,
            int index, boolean isSelected, boolean cellHasFocus) {
       

        def colors = [
            "modloader": [
                "default": [
                    "text": "#dd0404",
                    "left": new Color(221, 4, 4), /* RED */,
                    "middle": Color.WHITE,
                    "right" : Color.WHITE
                ],
                "selected": [
                    "text": "#dd0404",
                    "left": new Color(221, 4, 4) /* RED */,
                    "middle": new Color(234, 234, 234),
                    "right" : new Color(234, 234, 234)
                ],
                "focus": [

                ]
            ],
            "mod": [
                "default": [
                    "text": "#2c7fcc",
                    "left": new Color(44, 127, 204), /* BLUE */,
                    "middle": Color.WHITE,
                    "right" : Color.WHITE
                ],
                "selected": [
                    "text": "#2c7fcc",
                    "left": new Color(44, 127, 204), /* BLUE */,
                    "middle": new Color(234, 234, 234),
                    "right" : new Color(234, 234, 234)
                ]
            ],
            "texturepack": [
                "default": [
                    "text": new Color(58, 201, 75),
                    "left": new Color(58, 201, 75), /* GREEN */,
                    "middle": Color.WHITE,
                    "right" : Color.WHITE
                ],
                "selected": [
                    "text": new Color(58, 201, 75),
                    "left": new Color(58, 201, 75), /* GREEN */,
                    "middle": new Color(242, 242, 242),
                    "right" : new Color(242, 242, 242)
                ]
            ],
            "empty": [
                "default": [
                    "text": "#888888",
                    "left": new Color(86, 86, 86), /* GRAY */,
                    "middle": Color.WHITE,
                    "right" : Color.WHITE
                ],
                "selected": [
                    "text": "#888888",
                    "left": new Color(86, 86, 86) /* GRAY */,
                    "middle": new Color(234, 234, 234),
                    "right" : new Color(234, 234, 234)
                ],
                "focus": [

                ]
            ],

        ]

        def isInstalled = mainWindow?.getActiveProfile()?.hasDependency(pkg) 

        def defaultFontColor = colors."${pkg.type}"."default"."text"
        def selectedFontColor = colors."${pkg.type}"."selected"."text" ?: defaultFontColor

        if(isInstalled) {
            defaultFontColor = selectedFontColor = "#bbbbbb"
        }

        def defaultLeftColor = colors."${pkg.type}"."default"."left"
        def selectedLeftColor = colors."${pkg.type}"."selected"."left" ?: defaultLeftColor

        def defaultMiddleColor = colors."${pkg.type}"."default"."middle"
        def selectedMiddleColor = colors."${pkg.type}"."selected"."middle" ?: defaultMiddleColor

        def defaultRightColor = colors."${pkg.type}"."default"."right"
        def selectedRightColor = colors."${pkg.type}"."selected"."right" ?: defaultRightColor

        def defaultBorderColor = colors."${pkg.type}"."default"."border"?."color"
        def selectedBorderColor = colors."${pkg.type}"."selected"."border"?."color" ?: defaultBorderColor

        def emptyText = "<html><font size=\"4\">&nbsp;</font></html>"
        def text = "<html><p><font size=\"4\" color=\"${isSelected ? selectedFontColor : defaultFontColor }\"><b>${isSelected ? '<i>' : ''}${pkg.name} ${pkg.version}${isInstalled ? '&nbsp;&nbsp&nbsp( Installed )' : ''}${isSelected ? '</i>' : ''}</b></font></p><p>${pkg.type && pkg.type != 'empty' ? pkg.type : ''}</p></html>"

        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.LINE_AXIS));
        

        GradientLabel leftBorder = new GradientLabel(emptyText)
        leftBorder.start = leftBorder.end = new Color(25, 88, 183)
        leftBorder.setPreferredSize(new Dimension(10, 0))
        leftBorder.setBorder(BorderFactory.createEmptyBorder(12,0,12,0))

        GradientLabel textBlock = new GradientLabel(text)
        textBlock.start = textBlock.end = new Color(33, 117, 188)
        textBlock.setPreferredSize(new Dimension(340, 0))
        textBlock.setBorder(BorderFactory.createEmptyBorder(12,10,12,0))

        GradientLabel rightBorder = new GradientLabel(emptyText)
        rightBorder.start = rightBorder.end = new Color(80, 143, 196)
        rightBorder.setPreferredSize(new Dimension(10, 0))
        rightBorder.setBorder(BorderFactory.createEmptyBorder(12,0,12,0))

        if(isSelected) {
            if(selectedBorderColor) item.setBorder(BorderFactory.createMatteBorder(1,0,0,0, selectedBorderColor))
            leftBorder.start = leftBorder.end = selectedLeftColor
            textBlock.start = textBlock.end = selectedMiddleColor
            rightBorder.start = rightBorder.end = selectedRightColor
        } else {
            if(selectedBorderColor) item.setBorder(BorderFactory.createMatteBorder(1,0,0,0, defaultBorderColor))
            leftBorder.start = leftBorder.end = defaultLeftColor
            textBlock.start = textBlock.end = defaultMiddleColor
            rightBorder.start = rightBorder.end = defaultRightColor
        }

        item.add(leftBorder)
        item.add(textBlock)
        item.add(rightBorder)

        return item
    }
}