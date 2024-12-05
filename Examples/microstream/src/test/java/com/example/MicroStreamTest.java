package com.example;

import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = MicroStreamApplication.class)
public class MicroStreamTest {

    @Autowired
    private CarRepository carRepository;

    @Test
    void testMicroStream() {
        final EmbeddedStorageManager storageManager = EmbeddedStorage.start();

        CarPart porscheCarPart1 = new CarPart("wheel", 23.23);
        Car porsche = new Car("Porsche", "911", List.of(porscheCarPart1));

        storageManager.setRoot(porsche);
        storageManager.storeRoot();
        final Object root = storageManager.root();

        String porscheExpected = "Car{brand='Porsche', model='911', carPartList=[CarPart{name='wheel', price=23.23}]}";
        assertInstanceOf(Car.class, root);
        Car car = (Car) root;
        Assertions.assertEquals(porscheExpected, car.toString());

        storageManager.shutdown();
    }

    @Test
    void testMicroStreamSpringBoot() {
        carRepository.removeAll(); // To facilitate multiple test runs
        CarPart porscheCarPart1 = new CarPart("wheel", 23.23);
        Car porsche = new Car("Porsche", "911", List.of(porscheCarPart1));

        CarPart lamborghiniCarPart1 = new CarPart("mirror", 232.25);
        Car lamborghini = new Car("Lamborghini", "Diablo", List.of(lamborghiniCarPart1));

        CarPart paganiCarPart = new CarPart("engine", 8342.28);
        Car pagani = new Car("Pagani", "Huayra", List.of(paganiCarPart));

        List<Car> retrievedCarList = carRepository.findAll();
        assertEquals(0, retrievedCarList.size());

        carRepository.add(porsche);
        carRepository.add(lamborghini);
        carRepository.add(pagani);

        retrievedCarList = carRepository.findAll();
        assertEquals(3, retrievedCarList.size());
        String lamborghiniExpected = "Car{brand='Lamborghini', model='Diablo', carPartList=[CarPart{name='mirror', price=232.25}]}";
        Assertions.assertEquals(lamborghiniExpected, retrievedCarList.get(1).toString());

        carRepository.findAll().forEach(car -> car.setBrand(car.getBrand() + " rebranded"));
        carRepository.storeAll();

        retrievedCarList = carRepository.findAll();
        assertEquals(3, retrievedCarList.size());
        Assertions.assertEquals("Porsche rebranded", retrievedCarList.get(0).getBrand());
        Assertions.assertEquals("Lamborghini rebranded", retrievedCarList.get(1).getBrand());
        Assertions.assertEquals("Pagani rebranded", retrievedCarList.get(2).getBrand());
    }
}
