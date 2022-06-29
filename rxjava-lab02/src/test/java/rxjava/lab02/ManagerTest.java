package rxjava.lab02;

import static org.junit.Assert.*;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ManagerTest {
    Employee emp1, emp2, emp3, emp4, emp5, emp6, emp7;
    Manager mgr1, mgr2;
    List<Employee> mgr1Team, mgr2Team;

    @Before()
    public void setUp() {
        emp1 = new Employee("John", "Paxton", 50000);
        emp2 = new Employee("Angela", "Cortez", 65000);
        emp3 = new Employee("Jack", "Hawksmoor", 60000);
        emp4 = new Employee("Shen", "Li-Min", 70000);
        emp5 = new Employee("Jeroen", "Thornedike", 60000);
        emp6 = new Employee("Lucas", "Trent", 40000);
        emp7 = new Employee("Jenny", "Quantum", 80000);

        mgr1Team = new ArrayList<>(Arrays.asList(emp1, emp2, emp3));
        mgr2Team = new ArrayList<>(Arrays.asList(emp4, emp5, emp6));

        mgr1 = new Manager("Steve", "Topdog", 100000, mgr1Team);
        mgr2 = new Manager("Jenny", "Bigcheese", 100000, mgr2Team);
    }

    // Part 1
    @Test
    public void printOnlyFirstTwoEmployees() {
        Observable.concat(mgr1.getTeam().take(2), mgr2.getTeam().take(2)).forEach(System.out::println);
    }

    @Test
    public void concatBothManagersEmployees() {
        Observable<Employee> bothTeams = Observable.concat(mgr1.getTeam(), mgr2.getTeam());
        TestObserver<Employee> to = bothTeams.test();
        bothTeams.forEach(System.out::println);

        to.assertValues(emp1, emp2, emp3, emp4, emp5, emp6);
    }

    @Test
    public void printUntilHighPaidEmployee() {
        Observable<Employee> bothTeams = Observable.concat(mgr1.getTeam(), mgr2.getTeam());
        Observable<Employee> untilHighPaid = bothTeams.takeWhile(emp -> emp.getSalary().blockingGet() < 70_000);
        TestObserver<Employee> to = untilHighPaid.test();

        untilHighPaid.forEach(System.out::println);
        to.assertValues(emp1, emp2, emp3);
    }

    @Test
    public void sortEmployeesBySalary() {
        Observable<Employee> bothTeams = Observable.concat(mgr1.getTeam(), mgr2.getTeam());
        Observable<Employee> sorted = bothTeams.sorted(
                (empA, empB) -> Integer.compare(empA.getSalary().blockingGet(), empB.getSalary().blockingGet())
        );
        TestObserver<Employee> to = sorted.test();

        sorted.forEach(System.out::println);
        to.assertValues(emp6, emp1, emp3, emp5, emp2, emp4);
    }

    @Test
    public void filterForHighPaidEmployees() {
        Observable<Employee> bothTeams = Observable.concat(mgr1.getTeam(), mgr2.getTeam());
        Observable<Employee> onlyHighPaid = bothTeams.filter(emp -> emp.getSalary().blockingGet() >= 70_000);
        TestObserver<Employee> test = onlyHighPaid.test();

        onlyHighPaid.forEach(System.out::println);
        test.assertValues(emp4);
    }

    // Part 3
    @Test
    public void fetchEmployeesButThrowIfHighPaidAndComplete() {
        Observable<Employee> employeeObservable = mgr1.getTeam().doOnNext(employee -> {
            if (employee.getSalary().blockingGet() > 60_000) {
                throw new RuntimeException("Error! found a high paid employee!");
            }
        });

        employeeObservable.onErrorComplete().subscribe(System.out::println);
    }

    @Test
    public void fetchEmployeesButIfHighPaidThenInsertDummyEmployee() {
        Observable<Employee> employeeObservable = mgr1.getTeam().doOnNext(employee -> {
            if (employee.getSalary().blockingGet() > 60_000) {
                throw new RuntimeException("Error! found a high paid employee!");
            }
        }).onErrorReturnItem(new Employee("dummy", "bot", 0));

        employeeObservable.subscribe(System.out::println);
    }

    @Test
    public void fetchEmployeesButIfHighPaidThenThrowAndInsertDummyEmployee() {
        Observable<Employee> employeeObservable = mgr1.getTeam().map(employee -> {
            if (employee.getSalary().blockingGet() > 60_000) {
                return new Employee("dummy", "bot", 0);
            }
            return employee;
        });

        employeeObservable.subscribe(System.out::println);
    }

    @Test
    public void fetchEmployeesButThrowIfHighPaidAndThenSkipToNextEmployee() {
        Observable<Employee> bothTeamObservable = Observable.concat(mgr1.getTeam(), mgr2.getTeam());
        bothTeamObservable
                .map(employee -> {
                    if (employee.getSalary().blockingGet() > 60_000) {
                        return Observable.error(new RuntimeException("Error! found a high paid employee: " + employee));
                    }
                    return Observable.just(employee);
                })
                .flatMap(o -> o.onErrorResumeNext(e -> {
                    System.out.println("error was: " + e);
                    return Observable.empty();
                }))
                .subscribe(System.out::println);
    }
}

