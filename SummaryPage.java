package org.ecs160.a2;

import com.codename1.components.FloatingActionButton;
import com.codename1.io.Log;
import com.codename1.ui.*;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.layouts.Layout;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.plaf.UIManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class SummaryPage extends Form{

    public SummaryPage(Logic logic, UI parentForm) {
        this.logic = logic;
        setLayout(new BorderLayout());
        setTitle("Statistics");
        Button b1 = new Button("");
        b1.getAllStyles().setBorder(Border.createLineBorder(6, 0x3b4852));


        add(BorderLayout.NORTH, b1);

        Toolbar.setGlobalToolbar(true);
        Style s = UIManager.getInstance().getComponentStyle("Title");

        Form toolbar = new Form("Toolbar", new BoxLayout(BoxLayout.Y_AXIS));

        Button filterer = new Button("All Tasks");
        filterer.setIcon(FontImage.createMaterial(FontImage.MATERIAL_FILTER_ALT, s));
        toolbar.getToolbar().setTitleComponent(filterer);
        filterer.addActionListener((e) -> {
            changeSize(filterer);
            replace(getContentPane().getComponentAt(1),BuildStatsCnt(sizesForSummary[currentSizeNum]), CommonTransitions.createFade(5));
        });

        getToolbar().setBackCommand("",e -> {
            parentForm.showBack();
        });

        add(BorderLayout.NORTH, toolbar);

        add(BorderLayout.CENTER, BuildStatsCnt(sizesForSummary[currentSizeNum]));
    }

    private Container titleAndTime(String label, String timeStr) {
        Container container = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        container.add(new Label("\n\n"));
        container.add(whiteCenterLabel(label))
                .add(whiteCenterLabel(timeStr));
        container.getAllStyles().setFgColor(0x2e3030);
        container.getAllStyles().setBorder(Border.createLineBorder(6, 0x435059));
        container.getAllStyles().setBgTransparency(255);
        container.getAllStyles().setBgColor(0x6b7e8c);
        return container;
    }

    private Container top3TasksByValue(String label, ArrayList<String> sortedByValue) {
        Container container = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        container.add(new Label("\n"));
        container.add(whiteCenterLabel(label));
        for(int i = 0; i < sortedByValue.size(); i++) {
            container.add(whiteCenterLabel((i+1) +". " +sortedByValue.get(i)));
        }
        container.getAllStyles().setFgColor(0x2e3030);
        container.getAllStyles().setBorder(Border.createLineBorder(6, 0x435059));
        container.getAllStyles().setBgTransparency(255);
        container.getAllStyles().setBgColor(0x6b7e8c);
        return container;
    }

    private Label whiteCenterLabel(String input) {
        Label output = new Label(input);
        output.getStyle().setAlignment(CENTER);
        output.getStyle().setFgColor(0xffffff);
        output.getStyle().setFont(Font.createSystemFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE));
        return output;
    }

    private Container BuildStatsCnt(String size) {
        Container stats = new Container(new GridLayout(3, 2));

        HashMap<String, Integer> statMap = logic.SizeSummaryStatistics(sizesForSummary[currentSizeNum]);
        HashMap<String, String> minMax = logic.GetMinandMaxRuntimeTasks(sizesForSummary[currentSizeNum]);

        System.out.println(sizesForSummary[currentSizeNum]);
        Container totalTimeCnt = titleAndTime("Total Time:", logic.GenerateTimeStringFromSeconds(statMap.get("totalTime")));
        totalTimeCnt.add("_____________________________");
        totalTimeCnt.add(whiteCenterLabel("Number of Tasks:"));
        totalTimeCnt.add(whiteCenterLabel(statMap.get("numTasks")+""));

        Container meanTimeCnt = titleAndTime("Mean Time:", logic.GenerateTimeStringFromSeconds(statMap.get("meanTime")));

        Container minTimeCnt = titleAndTime("Min Time:", logic.GenerateTimeStringFromSeconds(statMap.get("minTime")));
        minTimeCnt.add(whiteCenterLabel(minMax.get("shortestTask")));

        Container maxTimeCnt = titleAndTime("Max Time:", logic.GenerateTimeStringFromSeconds(statMap.get("maxTime")));
        maxTimeCnt.add(whiteCenterLabel(minMax.get("longestTask")));


        Container newestTasks =
                top3TasksByValue("Newest Tasks",logic.Get3NewestTasks(sizesForSummary[currentSizeNum]));
        Container longestTasks =
                top3TasksByValue("Longest Tasks",logic.Get3LongestTasks(sizesForSummary[currentSizeNum]));

        stats.add(totalTimeCnt).add(meanTimeCnt).add(minTimeCnt).add(maxTimeCnt).add(newestTasks).add(longestTasks);
        return stats;
    }

    private int currentSizeNum = 0;

    public void changeSize(Button sizeButton) {
        do {
            currentSizeNum += 1;
            if (currentSizeNum == 5)
                currentSizeNum = 0;
        } while(logic.CountSizeClass(sizesForSummary[currentSizeNum]) == 0);

        String[] sizes = {"All Tasks", "S Tasks", "M Tasks", "L Tasks", "XL Tasks"};
        sizeButton.setText(sizes[currentSizeNum]);
    }

    private Logic logic;
    private String[] sizesForSummary = {"", "S", "M", "L", "XL"};
}
