(ns workshop.dataflow
  (:use [com.rpl.rama]
        [com.rpl.rama path]))

(comment
  (deframafn foo
    []
    (:> 1))

  ;;

  (deframafn bad-foo
    []
    (:> 1)
    (:> 2))

  ;;

  (deframafn bad-foo
    []
  )

  ;;

  (deframafn bad-foo
    []
    (:> 1)
    (println "B"))

  ;;

  (deframaop foo
    []
    (:> 1)
    (:> 2)
    (println "A")
    (:> 3))

  (?<-
   (foo :> *v)
   (println "B" *v))


  (?<-
   (println "B" (foo)))

  ;;


  (deframaop foo
    []
    (:> 1 2 3)
    (:> 4 5 6))

  (?<-
   (foo :> *v1 *v2 *v3)
   (println "OUT" *v1 *v2 *v3))

  ;;

  (deframafn foo
    [{:keys [*a *b] :as *m}]
    (println "A" *a)
    (println "B" *b)
    (println "M" *m)
    (:>))

  (foo {:a 1 :b 2})

  ;;


  (deframafn foo
    [*v]
    (<<if *v
      (println "true path")
      (:> 1)
     (else>)
      (println "false path")
      (:> 2)))

  ;;

  (deframafn foo
    [*v]
    (<<cond
     (case> (= *v 1))
      (println "case 1")
      (:> 1)

     (case> (= *v 2))
      (println "case 2")
      (:> 2)

     (default>)
      (println "default case")
      (:> 3)))


  ;;

  (deframafn foo
    [*x]
    (<<if (> *x 100)
      (:> *x)
     (else>)
      (:> (%self (* 2 *x)))))

  ;;

  (deframaop foo
    [*x]
    (loop<- [*i *x :> *v]
      (<<if (>= *i 0)
        (:> *i)
        (continue> (dec *i))
      ))
    (:> *v))

  (?<-
   (foo 5 :> *v)
   (println "V" *v))


  ;;

  (deframafn foo
    [*v]
    (<<ramafn %ret
      [*v2]
      (:> (+ *v *v2)))
    (:> %ret))

  ((foo 10) 5)

  ;;

  (deframafn foo
    [*v]
    (<<ramaop %ret
      [*v2]
      (:> (ops/range> 0 (* *v *v2))))
    (:> %ret))

  (?<-
   (foo 5 :> %f)
   (println (%f 2)))
)
