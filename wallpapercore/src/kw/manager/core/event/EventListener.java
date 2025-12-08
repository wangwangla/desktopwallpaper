package kw.manager.core.event;

import com.badlogic.gdx.utils.ArrayMap;

public class EventListener {
    private static EventListener listener;

    public static EventListener getInstance(){
        if (listener == null) {
            listener = new EventListener();
        }
        return listener;
    }

    private ArrayMap<String,EventRun> arrayMap = new ArrayMap<>();
    public void addEvent(String name,EventRun runnable){
        arrayMap.put(name,runnable);
    }

    public void removeEvent(String name){
        arrayMap.removeKey(name);
    }

    public void emit(String name,float x,float y){
        if (arrayMap.get(name)!=null) {
            arrayMap.get(name).run(x,y);
        }
    }
}
