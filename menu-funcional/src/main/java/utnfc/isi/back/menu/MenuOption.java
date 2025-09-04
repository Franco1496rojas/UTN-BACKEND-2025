package utnfc.isi.back.menu;

public class MenuOption {
    private final int code;
    private final String label;
    private final FuncAction action;

    public MenuOption(int code, String label, FuncAction action) {
        this.code = code;
        this.label = label;
        this.action = action;
    }

    public void execute(ApplicationContext ctx) {
        action.run(ctx);
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public FuncAction getAction() {
        return action;
    }
}
