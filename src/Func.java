import java.util.ArrayList;

public class Func {
    private ArrayList<String> params;
    private String def;

    public Func() {
        params = new ArrayList<>();
    }

    public void addParam(String param) {
        params.add(param);
    }

    public void setDef(String def) {
        this.def = def;
    }

    public String getDef() {
        return def;
    }

    public ArrayList<String> getParams() {
        return params;
    }
}
