package utnfc.isi.back.menu;

@FunctionalInterface

public interface FuncAction {
    //void run(); 
    void run(ApplicationContext ctx);
}
