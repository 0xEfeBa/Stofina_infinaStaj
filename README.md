# Stofina




Stofina, gücünü Java 17, Spring Boot 3 ve Spring Cloud ekosistemi üzerine kurulu modern bir mikroservis mimarisinden alır. Tüm sistem, Docker Compose ile tek bir komutla ayağa kaldırılabilecek şekilde tasarlanmıştır. Bu, geliştirme ve dağıtım süreçlerini oldukça basitleştirir.

Kullanıcı veya bir istemci uygulaması sisteme ilk adımını attığında, onu tüm istekler için birincil giriş kapısı olan `api-gateway` karşılar. Bu servis, sadece bir yönlendirici değil, aynı zamanda Redis destekli hız sınırlama (rate limiting) ve Resilience4j ile devre kesici (circuit breaker) mekanizmaları sayesinde sistemi dış tehditlere ve iç hatalara karşı koruyan akıllı bir bekçidir. Gateway, hangi isteğin hangi servise gideceğini ise mimarinin telefon rehberi olan `eureka-server`'a sorarak öğrenir.

Bir kullanıcı sisteme giriş yapmak istediğinde, `user-service` devreye girer. Spring Security ile güçlendirilen bu servis, kullanıcı kimliğini doğrular ve diğer servislerle güvenli iletişimi sağlayan bir JWT (JSON Web Token) üretir. Kullanıcının kimliği doğrulandıktan sonra, adres ve kimlik bilgileri gibi daha detaylı verileri `customer-service` yönetir.



Sistemin kalbi, alım-satım işlemlerinin gerçekleştiği döngüde atar. Bu döngünün merkezinde üç ana oyuncu bulunur:

1.  `market-data-service`: Canlı bir borsa simülasyonu gibi çalışarak, periyodik olarak hisse senedi fiyatlarını günceller. Bu güncellemeleri, sistemin ana damarlarından biri olan Apache Kafka'ya anlık olarak yayınlar. Aynı zamanda WebSocket üzerinden bu veriyi doğrudan kullanıcı arayüzüne ileterek ekranların sürekli canlı kalmasını sağlar.

2.  `portfolio-service`: Her kullanıcının finansal kalesidir. Kullanıcının nakit bakiyesini, sahip olduğu hisseleri ve alım emirleri için bloke edilen tutarları yönetir. Bir alım emri geldiğinde "Yeterli bakiye var mı?" veya bir satış emri geldiğinde "Bu hisseden yeterli adette var mı?" sorularını bu servis cevaplar.

3.  `order-service`: Orkestranın şefidir. Bir alım veya satım emri geldiğinde, önce `portfolio-service`'e danışarak bakiye/varlık kontrolü yapar. Ardından, `market-data-service`'den gelen ve Kafka üzerinden akan anlık fiyatları dinlemeye başlar. Emrin koşulları (örneğin, hedeflenen fiyata ulaşılması) sağlandığı an işlemi tetikler ve işlemin sonucunu (alım/satım gerçekleşti, para düşüldü, hisse eklendi vb.) onaylaması için `portfolio-service` ile tekrar konuşur. Bu servis, diğer servislerle olan iletişiminde yaşanabilecek anlık sorunlara karşı Resilience4j'in `Retry`, `Circuit Breaker` ve `Timeout` gibi mekanizmalarıyla donatılmıştır, bu da onu son derece dayanıklı kılar.

Bu ana döngüyü, kullanıcı kaydı veya başarılı işlemler sonrası `mail-service`'in Thymeleaf ile hazırlanan zengin HTML e-postaları göndermesi gibi yardımcı servisler destekler.


Stofina'yı bir staj projesinden daha fazlası yapan şey, "gözlemlenebilirlik" (observability) ve esnek çalışma ortamları gibi profesyonel özellikleridir. Grafana, Prometheus, Loki ve Tempo'dan oluşan tam bir izleme yığını, sistemin performans metriklerini, loglarını ve işlem akışlarını (tracing) tek bir arayüzden takip etme imkanı sunar. Bu, olası bir sorunu anında tespit edip müdahale etmeyi sağlar. Ayrıca, farklı senaryolar için hazırlanmış Spring Profilleri ve Docker Compose dosyaları, projenin bir geliştiricinin makinesinde yerel veritabanıyla veya tüm servislerin Docker üzerinde çalıştığı bir üretim ortamında kolayca ayağa kaldırılmasına olanak tanır.

Kısacası Stofina, modern yazılım mühendisliği prensiplerini ve en iyi pratikleri bir araya getirerek, hem teknik derinliği olan hem de hikayesiyle ilham veren başarılı bir projedir.
