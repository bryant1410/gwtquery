/*
 * Copyright 2011, The gwtquery team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.query.client;


import static com.google.gwt.query.client.GQuery.$;
import static com.google.gwt.query.client.GQuery.$$;
import static com.google.gwt.query.client.GQuery.document;

import com.google.gwt.core.client.Duration;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.query.client.Promise.Deferred;
import com.google.gwt.query.client.plugins.ajax.Ajax;
import com.google.gwt.query.client.plugins.deferred.Callbacks;
import com.google.gwt.query.client.plugins.deferred.Callbacks.Callback;
import com.google.gwt.query.client.plugins.deferred.PromiseFunction;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Test class for testing deferred and callbacks stuff.
 */
public class GQueryDeferredTestGwt extends GWTTestCase {
  
  static Element e = null;

  static HTML testPanel = null;

  public String getModuleName() {
    return "com.google.gwt.query.Query";
  }

  public void gwtTearDown() {
    $(e).remove();
    e = null;
  }

  public void gwtSetUp() {
    if (e == null) {
      testPanel = new HTML();
      RootPanel.get().add(testPanel);
      e = testPanel.getElement();
      e.setId("core-tst");
    } else {
      e.setInnerHTML("");
    }
  }

  private String result = "";
  public void testCallbacks() {
    Function fn1 = new Function() {
      public Object f(Object...arguments) {
        String s = " f1:";
        for (Object o: arguments){
          s += " " + o;
        }
        result += s;
        return false;
      }
    };
    
    Callback fn2 = new Callback() {
      public boolean f(Object... objects) {
        String s = " f2:";
        for (Object o: objects){
          s += " " + o;
        }
        result += s;
        return false;
      }
    };
    
    com.google.gwt.core.client.Callback<Object, Object> fn3 = new com.google.gwt.core.client.Callback<Object, Object>() {
      public void onFailure(Object reason) {
        result += " f3_fail: " + reason;
      }
      public void onSuccess(Object objects) {
        String s = " f3_success:";
        for (Object o: (Object[])objects){
          s += " " + o;
        }
        result += s;
      }
    };
    
    result = "";
    Callbacks callbacks = new Callbacks();
    callbacks.add( fn1 );
    callbacks.fire( "foo" );
    assertEquals(" f1: foo", result);
    
    result = "";
    callbacks.add( fn2 );
    callbacks.fire( "bar" );
    assertEquals(" f1: bar f2: bar", result);

    result = "";
    callbacks.remove( fn2 );
    callbacks.fire( "foobar" );
    assertEquals(" f1: foobar", result);

    result = "";
    callbacks.add( fn1 );
    callbacks.fire( "foo" );
    assertEquals(" f1: foo f1: foo", result);

    result = "";
    callbacks = new Callbacks("unique");
    callbacks.add( fn1 );
    callbacks.add( fn1 );
    callbacks.fire( "foo" );
    assertEquals(" f1: foo", result);

    result = "";
    callbacks.add( fn3 );
    callbacks.fire( "bar" );
    assertEquals(" f1: bar f3_success: bar", result);
    
    result = "";
    callbacks = new Callbacks("memory");
    callbacks.add( fn1 );
    callbacks.fire( "foo" );
    callbacks.add( fn2 );
    callbacks.fire( "bar" );
    callbacks.remove(fn2);
    callbacks.fire( "foobar" );
    assertEquals(" f1: foo f2: foo f1: bar f2: bar f1: foobar", result);

    result = "";
    callbacks = new Callbacks("stopOnFalse");
    callbacks.add( fn1 );
    callbacks.add( fn2 );
    callbacks.fire( "bar" );
    assertEquals(" f1: bar", result);
    
    result = "";
    callbacks.disable();
    callbacks.fire( "bar" );
    assertEquals("", result);

    result = "";
    callbacks = new Callbacks("memory once unique");
    callbacks.add( fn1 );
    callbacks.add( fn1 );
    callbacks.fire( "bar" );
    assertEquals(" f1: bar", result);
    callbacks.fire( "foo" );
    assertEquals(" f1: bar", result);
    callbacks.add( fn2 );
    callbacks.add( fn2 );
    assertEquals(" f1: bar f2: bar f2: bar", result);
    callbacks.remove( fn1 );
    callbacks.add( fn1 );
    assertEquals(" f1: bar f2: bar f2: bar f1: bar", result);
    callbacks.remove( fn1 );
    callbacks.disable();
    callbacks.add( fn1 );
    assertEquals(" f1: bar f2: bar f2: bar f1: bar", result);
  }
  
  public void testDeferredAjaxWhenDone() {
    String url = "https://www.googleapis.com/blogger/v2/blogs/user_id/posts/post_id?callback=?&key=NO-KEY";
    
    delayTestFinish(5000);
    GQuery.when(Ajax.getJSONP(url, null, null, 1000))
      .done(new Function() {
        public void f() {
          Properties p = getArgument(0, 0);
          assertEquals(400, p.getProperties("error").getInt("code"));
          finishTest();
        }
      });
  }

  public void testDeferredAjaxWhenFail() {
    String url1 = "https://www.googleapis.com/blogger/v2/blogs/user_id/posts/post_id?callback=?&key=NO-KEY";
    String url2 = "https://localhost:4569/foo";
    
    delayTestFinish(5000);
    GQuery.when(
        Ajax.getJSONP(url1), 
        Ajax.getJSONP(url2, null, null, 1000))
      .done(new Function() {
        public void f() {
          fail();
        }
      })
      .fail(new Function(){
        public void f() {
          finishTest();
        }
      });
  }
  
  int progress = 0;
  public void testPromiseFunction() {
    delayTestFinish(3000);
    final Promise doSomething = new PromiseFunction() {
      @Override
      public void f(final Deferred dfd) {
        new Timer() {
          int count = 0;
          public void run() {
            dfd.notify(count ++);
            if (count > 3) {
              cancel();
              dfd.resolve("done");
            }
          }
        }.scheduleRepeating(50);
      }
    };
    
    doSomething.progress(new Function() {
      public void f() {
        progress = getArgument(0);
      }
    }).done(new Function() {
      public void f() {
        assertEquals(3, progress);
        assertEquals(Promise.RESOLVED, doSomething.state());
        finishTest();
      }
    });
  }
  
  public void testNestedPromiseFunction() {
    progress = 0;
    delayTestFinish(3000);
    
    Promise doingFoo = new PromiseFunction() {
      public void f(final Deferred dfd) {
        new Timer() {
          int count = 0;
          public void run() {
            dfd.notify(count ++);
            if (count > 3) {
              cancel();
              dfd.resolve("done");
            }
          }
        }.scheduleRepeating(50);
      }
    };
    
    Promise doingBar = new PromiseFunction() {
      public void f(final Deferred dfd) {
        new Timer() {
          int count = 0;
          public void run() {
            dfd.notify(count ++);
            if (count > 3) {
              cancel();
              dfd.resolve("done");
            }
          }
        }.scheduleRepeating(50);
      }
    };
    
    GQuery.when(doingFoo, doingBar).progress(new Function() {
      public void f() {
        int c = getArgument(0);
        progress += c;
      }
    }).done(new Function() {
      public void f() {
        assertEquals(12, progress);
        finishTest();
      }
    });
  }

  public void testThen() {
    new PromiseFunction() {
      public void f(final Deferred dfd) {
        dfd.resolve(5);
      }
    }.done(new Function() {
      public void f() {
        assertEquals(5d, arguments(0));
      }
    }).then(new Function() {
      public Object f(Object... args) {
        return (Double)args[0] * 2;
      }
    }).done(new Function() {
      public void f() {
        assertEquals(10d, arguments(0));
      }
    });
  }

  public void testDeferredAjaxThenDone() {
    final String url = "https://www.googleapis.com/blogger/v2/blogs/user_id/posts/post_id?callback=?&key=NO-KEY";

    delayTestFinish(5000);
    GQuery
      .when(Ajax.getJSONP(url))
      .then(new Function() {
        public Object f(Object... args) {
          Properties p = arguments(0, 0);
          assertEquals(400, p.getProperties("error").getInt("code"));
          return Ajax.getJSONP(url);
        }
      })
      .done(new Function() {
        public void f() {
          Properties p = arguments(0, 0);
          assertEquals(400, p.getProperties("error").getInt("code"));
          finishTest();
        }
      });
  }
  
  public void testDeferredAjaxThenFail() {
    delayTestFinish(5000);
    GQuery
      .when(new PromiseFunction() {
        public void f(Deferred dfd) {
          dfd.resolve("message");
        }
      })
      .then(new Function() {
        public Object f(Object... args) {
          return new PromiseFunction() {
            public void f(Deferred dfd) {
              dfd.resolve(arguments);
            }
          };
        }
      })
      .then(new Function() {
        public Object f(Object... args) {
          return new PromiseFunction() {
            public void f(Deferred dfd) {
              dfd.reject(arguments);
            }
          };
        }
      })
      .done(new Function() {
        public void f() {
          finishTest();
          fail();
        }
      })
      .fail(new Function() {
        public void f() {
          assertEquals("message", arguments(0));
          finishTest();
        }
      });
  }

  public void testDeferredQueueDelay() {
    final int delay = 300;
    final double init = Duration.currentTimeMillis();
    
    delayTestFinish(delay * 2);
    
    Function doneFc = new Function() {
      public void f() {
        finishTest();

        double ellapsed = Duration.currentTimeMillis() - init;
        assertTrue(ellapsed >= delay);
      }
    };
    
    $(document).delay(delay).promise().done(doneFc);
  }
  
  int deferredRun = 0;
  public void testDeferredQueueMultipleDelay() {
    final int delay = 300;
    final double init = Duration.currentTimeMillis();
    deferredRun = 0;
    
    delayTestFinish(delay * 3);
    
    $("<div>a1</div><div>a2</div>")
      .delay(delay, new Function() {
        public void f() {
          double ellapsed = Duration.currentTimeMillis() - init;
          assertTrue(ellapsed >= delay);
          deferredRun ++;
        }
      })
      .delay(delay, new Function() {
        public void f() {
          double ellapsed = Duration.currentTimeMillis() - init;
          assertTrue(ellapsed >= (delay * 2));
          deferredRun ++;
        }
      })
      .promise().done(new Function() {
        public void f() {
          finishTest();
          // Functions are run 4 times (2 functions * 2 elements) 
          assertEquals(4, deferredRun);
        }
      });
  }
  
  /**
   * Example taken from the gquery.promise() documentation
   */
  public void testDeferredEffect() {
    $(e).html("<button>click</button><p>Ready...</p><br/><div></div>");
    $("div", e).css($$("height: 50px; width: 50px;float: left; margin-right: 10px;display: none; background-color: #090;"));
    
    final Function effect = new Function() {public Object f(Object... args) {
        return $("div", e).fadeIn(800).delay(1200).fadeOut();
    }};
    
    final double init = Duration.currentTimeMillis();

    delayTestFinish(10000);

    $("button", e)
      .click(new Function(){public void f()  {
        $("p", e).append(" Started... ");
        GQuery.when( effect ).done(new Function(){public void f()  {
          $("p", e).append(" Finished! ");
          assertEquals("Ready... Started...  Finished! ", $("p", e).text());
          
          double ellapsed = Duration.currentTimeMillis() - init;
          assertTrue(ellapsed >= (800 + 1200 + 400));
          
          finishTest();
        }});
      }})
    .click();
  }
  
  /**
   * Example taken from the gquery.promise() documentation
   */
  public void testDeferredEffectEach() {
    $(e).html("<button>click</button><p>Ready...</p><br/><div></div><div></div><div></div><div></div>");
    $("div", e).css($$("height: 50px; width: 50px;float: left; margin-right: 10px;display: none; background-color: #090;"));
    
    final double init = Duration.currentTimeMillis();

    delayTestFinish(10000);

    $("button", e)
      .bind("click", new Function(){public void f()  {
        $("p", e).append(" Started... ");
        
        $("div",e).each(new Function(){public Object f(Element e, int i) {
          return $( this ).fadeIn().fadeOut( 1000 * (i+1) );
        }});
        
        $("div", e).promise().done(new Function(){ public void f() {
          $("p", e).append( " Finished! " );
          
          assertEquals("Ready... Started...  Finished! ", $("p", e).text());
          double ellapsed = Duration.currentTimeMillis() - init;
          assertTrue(ellapsed >= (1000 * 4));
          
          finishTest();
        }});
      }})
     .click();
  }
  
  public void testWhenArgumentsWhithAnyObject() {
    $(e).html("<div>a1</div><div>a2</div>");
    
    final GQuery g = $("div", e);
    assertEquals(2, g.length());
    
    // We can pass to when any object.
    GQuery.when(g, g.delay(100).delay(100), "Foo", $$("{bar: 'foo'}"))
          .done(new Function(){public void f() {
              GQuery g1 = arguments(1, 0);
              GQuery g2 = arguments(1, 0);
              String foo = arguments(2, 0);
              Properties p = arguments(3, 0);
              
              // We dont compare g and g1/g2 because they are different
              // objects (GQuery vs QueuePlugin) but we can compare its content
              assertEquals(g.toString(), g1.toString());
              assertEquals(g.toString(), g2.toString());
              
              assertEquals("Foo", foo);
              assertEquals("foo", p.get("bar"));
          }});
  }
}