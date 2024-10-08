## 동시성 제어 테스트에 대한 분석 보고서

### 전제 
- 포인트는 개별적으로 운용하므로 재고 시스템과 달리 타유저 동시 접근이 적용되지 않는다.        
- 따라서 동일한 유저에 대한 동시 충전/사용에 대한 정책이 필요.       
- Case 1 . 동일 유저 동시 포인트 충전        
- Case 2 . 동일 유저 동시 포인트 사용       
- Case 3 . 동일 유저 동시 포인트 사용 시 잔액 부족으로 인한 실패       

### case 1. 동일 유저 포인트 동시 충전
- 유저 A의 1000원, 2000원 포인트 충전이 동시에 실행된 경우 -> 총 3000원의 금액이 충전 완료 되어야함.          
  
  ```java
         @Test
        void 포인트_동일유저_동시충전() throws InterruptedException {
            //given
            pointService.charge(1L, 10000);

            //when
            Runnable A = () -> {
                UserPoint chargePoint = pointService.charge(1L, 1000);
                System.out.println("point runable A " + chargePoint.point());
            };

            Runnable B = () -> {
                UserPoint chargePoint = pointService.charge(1L, 2000);
                System.out.println("point runable B " + chargePoint.point());
            };

            CompletableFuture.runAsync(A).thenCompose((a) -> CompletableFuture.runAsync(B)).join();

            Thread.sleep(1000);

            //then
            UserPoint userPoint = pointService.getPointById(1L);
            assertEquals(10000 + 1000 + 2000, userPoint.point());
        }
  ```
### case 2. 동일 유저 포인트 동시 사용
- 유저 A의 포인트 1000원, 500원 사용이 동시에 실행된 경우 -> 총 1500원의 포인트 사용이 완료 되어야 함.         

  ```java
        @Test
        void 포인트_동일유저_동시사용() throws InterruptedException {
            //given
            pointService.charge(1L, 10000);

            //when
            Runnable A = () -> {
                try {
                    UserPoint usingPoint = pointService.use(1L, 1000);
                    System.out.println("point runable A " + usingPoint.point());
                } catch (PointShortageException e) {
                    throw new RuntimeException(e);
                }
            };

            Runnable B = () -> {
                try {
                    UserPoint usingPoint = pointService.use(1L, 500);
                    System.out.println("point runable B " + usingPoint.point());
                } catch (PointShortageException e) {
                    throw new RuntimeException(e);
                }
            };

            CompletableFuture.runAsync(A).thenCompose((a) -> CompletableFuture.runAsync(B)).join();

            Thread.sleep(1000);

            //then
            UserPoint userPoint = pointService.getPointById(1L);
            assertEquals(10000 - 1000 - 500, userPoint.point());
        }
  ```


  ### case 3. case 2에서 포인트 잔액이 부족하여 실패한 경우
  - 유저 A의 포인트 사용이 동시에 일어났으나 잔액 부족으로 실패하면 -> 사용은 중지되고 실패 이전의 포인트가 유지되어야 함.            
 
    ```java
        @Test
        public void 포인트_동일유저_동시사용_포인트_부족한_경우() throws InterruptedException {
            //given
            long amount = 5000;
            pointService.charge(1L, 10000);

            int threadCount = 3;

            ExecutorService executorService = Executors.newFixedThreadPool(3);

            CountDownLatch latch = new CountDownLatch (threadCount);

            //when
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    try {
                        synchronized (this)
                        {
                            UserPoint userPoint = pointService.getPointById(1L);
                            if(userPoint.point() < amount) {
                                throw new PointShortageException("포인트가 부족합니다.");
                            }
                            pointService.use(1L, amount);
                        }

                    } catch (PointShortageException e) {
                        throw new RuntimeException(e);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();

            //then
            UserPoint userPoint = pointService.getPointById(1L);
            assertEquals(0, userPoint.point());
        }
    }
  ```

### 결과 분석 및 회고
- case 1에서 동일한 유저에 대한 포인트 동시 충전 시 모두 충전 완료됨을 확인.     
- case 2에서 동일한 유저에 대한 포인트 동시 사용 시 모두 정상 사용 완료됨을 확인.    
- case 3에서 10000원 충전되어 있는 유저에 대해 5000원 포인트 사용이 세번 동시에 이루어 진 경우, 두번만 성공하고 세번째 실패 시도 이전 포인트가 유지됨을 확인함.         
![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FcSub6M%2FbtsJNBA32Z2%2FN2NvaVkceeZvq5RWWZLwaK%2Fimg.png)


- 이번 프로젝트의 케이스는 동일한 유저에 대한 동시 접근을 제어하기에 비교적 케이스가 단순하고 수월하였으나, 그럼에도 다양한 동시성 제어 방식, 이를 테스트 하는 방식에 대한 학습 필요성을 느낌.

