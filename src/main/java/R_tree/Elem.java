package R_tree;

import common.RectItem;
import geometry.Rectangle;

import java.util.Objects;

public class Elem<T extends RectItem> {
    public T data;
    public Rectangle MBR;

    public Elem(T data) {
        this.data = data;
        this.MBR = data.getRectangle();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Elem<?> elem = (Elem<?>) o;
        return Objects.equals(data, elem.data) &&
                Objects.equals(MBR, elem.MBR);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, MBR);
    }
}
