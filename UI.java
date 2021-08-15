package org.ecs160.a2;

import com.codename1.components.*;
import com.codename1.ui.*;
import com.codename1.ui.layouts.*;
import com.codename1.ui.plaf.*;
import com.codename1.ui.table.TableLayout;
import java.sql.*;
import java.util.Vector;

import com.codename1.ui.Component;
import com.codename1.ui.FontImage;

// Main UI for the Timer App
public class UI extends Form {

    public Container taskList;
    public Animator anim;
    public Logic logic;

    public UI() {

        anim = new Animator();
        logic = new Logic();
        setTitle("Time Tracker");
        setLayout(new BorderLayout());

        createTaskList();

        drawTasks();

        Container blankSpace = createSpacing();
        createNewTaskButton();
        createStatPageButton();

        getToolbar().addSearchCommand(e -> search((String)e.getSource()));


        setScrollable(false);
        add(BorderLayout.NORTH, taskList);
        add(BorderLayout.CENTER, blankSpace);

    }

    public void drawTasks() {
        try {
            ResultSet rs = logic.GetAllTasks();
            while (rs.next()) {
                String name = rs.getString("name");
                String size = rs.getString("size");
                String description = rs.getString("description");
                int runTime = rs.getInt("runTime");

                taskList.add(DisplayTask(name, size, description, runTime));
                taskList.animateLayout(5);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private Container createSpacing(){
        Container blankSpace = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        blankSpace.getAllStyles().setFgColor(0x99a1a1);
        blankSpace.getAllStyles().setBgTransparency(255);
        blankSpace.getAllStyles().setBgColor(0x37454f);
        return blankSpace;
    }

    private void createNewTaskButton() {
        FloatingActionButton fab = makeFAB(FontImage.MATERIAL_ADD);
        fab.bindFabToContainer(getContentPane());
        fab.addActionListener(e -> {
            Dialog popup = taskPopup();
            popup.show();
        });
    }

    private void createStatPageButton() {
        FloatingActionButton fab = makeFAB(FontImage.MATERIAL_ASSESSMENT);
        Container layer = getLayeredPane(getClass(), true);
        FlowLayout flow = new FlowLayout(RIGHT);
        flow.setValign(BOTTOM);
        layer.setLayout(flow);
        layer.add(fab);
        fab.getAllStyles().setMarginUnit(Style.UNIT_TYPE_DIPS);
        fab.getAllStyles().setMarginBottom(15);
        fab.addActionListener(e -> {
            if (logic.CountAllTasks() > 1) {
                new SummaryPage(logic, this).show();
                anim.updateTimers();
            } else {
                ToastBar.Status status = ToastBar.getInstance().createStatus();
                status.setMessage("Statistics are available for 2 or more tasks. \n" +
                        "Create a new task with the + button.");
                status.setExpires(5000);
                status.show();
            }
        });
    }

    private FloatingActionButton makeFAB(char icon) {
        FloatingActionButton fab = FloatingActionButton.createFAB(icon);
        fab.getAllStyles().setFgColor(0x2e3030);
        fab.getAllStyles().setBgColor(0x6b7e8c);
        fab.getAllStyles().setBgTransparency(255);
        return fab;
    }

    private void createTaskList() {
        taskList = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        taskList.setScrollableY(true);
        taskList.getAllStyles().setFgColor(0x6b7e8c);
        taskList.getAllStyles().setBgTransparency(255);
        taskList.getAllStyles().setBgColor(0x37454f);
        taskList.getStyle().setPadding(20, 20, 20, 20);
        taskList.getAllStyles().setBorder(Border.createLineBorder(6, 0x37454f));
    }

    private void search(String text) {
        if(text == null || text.length() == 0) {
            for(Component c : taskList) {
                c.setHidden(false);
                c.setVisible(true);
            }
        } else {
            for(Component c : taskList) {
                text = text.toLowerCase();
                boolean show = c.getName().toLowerCase().contains(text);
                c.setHidden(!show);
                c.setVisible(show);
            }
        }
        getContentPane().animateLayout(200);
    }

    private String setFreeName() {
        String setName = "";
        int taskGuess = 1;
        while (setName.equals("")) {
            setName = "Task " + taskGuess;
            if (logic.TaskExists(setName)) {
                setName = "";
                taskGuess += 1;
            }
        }
        return setName;
    }

    private Dialog taskPopup() {
        Dialog popup = new Dialog(new BoxLayout(BoxLayout.Y_AXIS));

        TableLayout tl = new TableLayout(4,1);
        Container instructions = new Container(tl);
        Label instruction = new Label("Enter New Task Name:");
        instruction.getStyle().setAlignment(CENTER);
        instructions.add(instruction);

        Label warning = new Label("");
        warning.getStyle().setAlignment(CENTER);
        warning.getStyle().setFgColor(0xff0000);

        TextField nameBar = new TextField();
        nameBar.setHint(setFreeName());

        Label instruction2 = new Label("Enter Optional Description:");
        TextArea descBar= new TextArea(7, 30);
        descBar.setHint("Description");

        GridLayout gl = new GridLayout(1,2);
        Container buttons = new Container(gl);
        Button confirmButton = new Button("Confirm");
        Button cancelButton = new Button("Cancel");
        buttons.add(confirmButton);
        buttons.add(cancelButton);

        confirmButton.addActionListener(ev -> {
            if (!nameBar.getText().equals("")) {
                if (logic.TaskExists(nameBar.getText())) {
                    nameBar.setText("");
                    warning.setText("That task name is already in use.");
                    instructions.add(warning);
                    popup.growOrShrink();
                } else {
                    taskList.add(addTask(nameBar.getText(),descBar.getText()));
                    taskList.animateLayout(5);
                    anim.updateTimers();
                    popup.dispose();
                }
            }
            else {
                taskList.add(addTask(nameBar.getHint(), descBar.getText()));
                taskList.animateLayout(5);
                anim.updateTimers();
                popup.dispose();
            }
        });

        cancelButton.addActionListener(ex -> {
            if(!nameBar.getText().isEmpty() || !descBar.getText().isEmpty()) {
                discardConfirmation(popup).show();
            } else {
                anim.updateTimers();
                popup.dispose();
            }
        });

        popup.add(instructions)
                .add(nameBar)
                .add(instruction2).add(descBar)
                .add(buttons);

        return popup;
    }

    private Dialog discardConfirmation(Dialog parentPopup) {
        Dialog popup = new Dialog(new BorderLayout());
        popup.getAllStyles().setFgColor(0x2e3030);
        popup.getAllStyles().setBorder(Border.createLineBorder(6, 0x435059));
        popup.getAllStyles().setBgTransparency(255);
        popup.getAllStyles().setBgColor(0x6b7e8c);

        Label instruction = new Label("Discard Unsaved Changes?");
        instruction.getStyle().setAlignment(CENTER);

        //Container buttons = new Container(new GridLayout(1,2));
        Container buttons = new Container(new GridLayout(1,2));
        Button cancelButton = new Button("No");
        Button confirmButton = new Button("Yes");


        buttons.add(confirmButton);
        buttons.add(cancelButton);


        confirmButton.addActionListener(ev -> {
            anim.updateTimers();
            parentPopup.dispose();
            popup.dispose();
        });

        cancelButton.addActionListener(ex -> popup.dispose());

        popup.add(BorderLayout.NORTH,instruction).add(BorderLayout.CENTER,buttons);

        return popup;
    }

    public Task addTask(String name, String description) {
        logic.CreateNewTask(name, "S");

        return DisplayTask(name, "S", description,0);
    }

    public Task DisplayTask(String name, String size, String description, int runTime) {
        // Initialization for task viewer
        TableLayout tl = new TableLayout(1, 4);

        Task upper = newStyledTask(tl, name, description);

        // Initialization for size, name, and time buttons
        upper.add(tl.createConstraint().widthPercentage(20), upper.createSizeButton(size));
        upper.add(tl.createConstraint().widthPercentage(60), upper.createAccordion(description));
        upper.add(tl.createConstraint().widthPercentage(-2), upper.createTimeButton(runTime));

        return upper;
    }

    private Task newStyledTask(Layout tl, String name, String description) {
        Task upper = new Task(tl, name);

        Stroke borderStroke = new Stroke(2, Stroke.CAP_SQUARE, Stroke.JOIN_MITER, 1);


        upper.getStyle().setBgTransparency(255);
        upper.getStyle().setBgColor(0xc0c0c0);
        upper.getStyle().setPadding(10, 10, 10, 10);
        upper.getStyle().setBorder(Border.createEtchedLowered());
        upper.getAllStyles().setFgColor(0x2e3030);
        upper.getUnselectedStyle().setBorder(RoundRectBorder.create().
                strokeColor(0).
                strokeOpacity(20).
                stroke(borderStroke));
        upper.getAllStyles().setMarginUnit(Style.UNIT_TYPE_DIPS);
        upper.getAllStyles().setMargin(Component.BOTTOM, 1);
        upper.setName(name+description);

        return upper;
    }

    public class Animator {
        Vector<TimeDisplay> activeTasks = new Vector<>();
        int currentActive = 0;
        Logic logic = new Logic();

        public void addAnimated(TimeDisplay timer) {
            registerAnimated(timer);
            activeTasks.add(timer);
            currentActive += 1;
        }

        public void removeAnimated(TimeDisplay timer) {
            deregisterAnimated(timer);
            activeTasks.remove(timer);
            currentActive -= 1;
        }

        public void updateTimers() {
            if (currentActive > 0) {
                for (TimeDisplay timer : activeTasks) {
                    deregisterAnimated(timer);
                    String taskName = timer.taskName;
                    int runTime = logic.GetRuntime(taskName) + logic.CalculateTimeDifference(taskName);
                    System.out.print(runTime);
                    timer.updateTimeStamp(runTime);
                    registerAnimated(timer);
                }
            }
        }
    }

    public class Task extends Container {
        String taskName;
        int currentSize;
        String description;
        int runTime;
        TimeDisplay timer;

        public Task(Layout t1, String name) {
            super(t1);
            currentSize = 0;
            taskName = name;
            description = "";
            runTime = 0;
        }

        public Button createSizeButton(String sizeStr) {
            Button size = new Button(sizeStr);
            size.getStyle().setAlignment(CENTER);
            size.getAllStyles().setFgColor(0x2e3030);
            size.getAllStyles().setBgTransparency(0);
            size.getAllStyles().setBgColor(0x6b7e8c);

            size.addActionListener((e) -> changeSize(size));

            return size;
        }

        public void changeSize(Button sizeButton) {
            currentSize += 1;
            if (currentSize == 4)
                currentSize = 0;
            String[] sizes = {"S", "M", "L", "XL"};
            sizeButton.setText(sizes[currentSize]);
            logic.ResizeTask(taskName,sizes[currentSize]);
        }

        public TimeDisplay createTimeButton(int runTime) {
            TimeDisplay time = new TimeDisplay(runTime, taskName);
            time.getStyle().setAlignment(CENTER);
            time.getAllStyles().setFgColor(0x752c29);
            time.getAllStyles().setBgTransparency(0);
            time.getAllStyles().setBgColor(0x752c29);

            time.addActionListener((e) -> toggleRunning(time));
            timer = time;
            return time;
        }

        private void toggleRunning(TimeDisplay time) {
            if (time.timerRunning) {
                time.stop();
                logic.StopTask(taskName);
                anim.removeAnimated(time);
            }
            else {
                time.start();
                logic.StartTask(taskName);
                    anim.addAnimated(time);
            }
        }

        public Component createAccordion(String description) {
            Accordion accr = new Accordion();
            BorderLayout bl = new BorderLayout();
            Container body = new Container(bl);

            TextArea ta = new TextArea(6, 40);
            boolean isDefaultDescription = description == null || description.isEmpty();
            if(isDefaultDescription) {
                ta.setHint("Description");
            } else {
                ta.setText(description);
                sendDesc(ta);
            }

            ta.getAllStyles().setBgTransparency(255);
            ta.getAllStyles().setFont(Font.createSystemFont(1,1,8));

            TableLayout tl = new TableLayout(1,2);
            Container RenameBox = new Container(tl);
            Button renamer = new Button("Rename");
            TextField nameSet = new TextField();
            RenameBox.add(tl.createConstraint().widthPercentage(55), nameSet);
            RenameBox.add(tl.createConstraint().widthPercentage(-2), renamer);

            Label vText = new Label("Sample Text");
            vText.getStyle().setFgColor(0x000000);
            vText.getStyle().setFont(Font.createSystemFont(1,1, 0));
            vText.getStyle().setAlignment(CENTER);

            body.add(BorderLayout.NORTH, vText);
            body.add(BorderLayout.CENTER, ta);
            body.add(BorderLayout.SOUTH, RenameBox);

            accr.addContent(taskName, body);
            accr.setScrollableY(false);

            renamer.addActionListener((e) -> {
                changeName(nameSet.getText());
                accr.setHeader(taskName, body);
                nameSet.clear();
            });
            accr.addOnClickItemListener((e) -> vText.setText(timer.getVerbose()));
            ta.addCloseListener((e) -> sendDesc(ta));

            return accr;
        }

        private void changeName(String newName) {
            if (!logic.TaskExists(newName)) {
                logic.RenameTask(taskName,newName);
                taskName = newName;
            }
        }

        private void sendDesc(TextArea ta) {
            logic.SetTaskDescription(taskName, ta.getText());
        }
    }

    public void show() {
        super.show();
    }
}