package gwtquery.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.animation.client.Animation;

public class Effects extends GQuery {

  static {
    GQuery.registerPlugin(Effects.class, new EffectsPlugin());
  }

  public static final Class<Effects> Effects = Effects.class;

  public Effects(Element element) {
    super(element);
  }

  public Effects(JSArray elements) {
    super(elements);
  }

  public Effects(NodeList list) {
    super(list);
  }

  public Effects fadeOut() {
    Animation a = new Animation() {

      public void onCancel() {
      }

      public void onComplete() {
        for (int i = 0; i < elements.getLength(); i++) {
          elements.getItem(i).getStyle().setProperty("opacity", "0");
          elements.getItem(i).getStyle().setProperty("display", "none");
        }
      }

      public void onStart() {
      }

      public void onUpdate(double progress) {
        for (int i = 0; i < elements.getLength(); i++) {
          elements.getItem(i).getStyle()
              .setProperty("opacity", String.valueOf(1.0 - progress));
        }
      }
    };
    a.run(1000);
    return this;
  }

  public Effects fadeIn() {
    Animation a = new Animation() {

      public void onCancel() {
      }

      public void onComplete() {
      }

      public void onStart() {
      }

      public void onUpdate(double progress) {
        for (int i = 0; i < elements.getLength(); i++) {
          elements.getItem(i).getStyle()
              .setProperty("opacity", String.valueOf(progress));
        }
      }
    };
    a.run(1000);
    return this;
  }

  public static class EffectsPlugin implements Plugin<Effects> {

    public Effects init(GQuery gq) {
      return new Effects(gq.get());
    }
  }
}