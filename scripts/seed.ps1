$ErrorActionPreference = "Stop"

$dataDir = Join-Path $PSScriptRoot "..\data"
if (-not (Test-Path $dataDir)) {
    New-Item -ItemType Directory -Force -Path $dataDir | Out-Null
}

$today = Get-Date
$fmt = "yyyy-MM-dd"
$fmtTime = "yyyy-MM-ddTHH:mm:ss"

$dProdPast60 = $today.AddDays(-60).ToString($fmt)
$dProdPast30 = $today.AddDays(-30).ToString($fmt)
$dProdPast15 = $today.AddDays(-15).ToString($fmt)
$dProdPast5  = $today.AddDays(-5).ToString($fmt)
$dProdPast2  = $today.AddDays(-2).ToString($fmt)

$dImportPast45 = $today.AddDays(-45).ToString($fmt)
$dImportPast20 = $today.AddDays(-20).ToString($fmt)
$dImportPast10 = $today.AddDays(-10).ToString($fmt)
$dImportPast3  = $today.AddDays(-3).ToString($fmt)
$dImportPast1  = $today.AddDays(-1).ToString($fmt)

$dExpActiveLong  = $today.AddDays(180).ToString($fmt)
$dExpActiveMed   = $today.AddDays(60).ToString($fmt)
$dExpActiveShort = $today.AddDays(14).ToString($fmt)
$dExpNear1       = $today.AddDays(2).ToString($fmt)
$dExpNear2       = $today.AddDays(5).ToString($fmt)
$dExpExpired1    = $today.AddDays(-3).ToString($fmt)
$dExpExpired2    = $today.AddDays(-12).ToString($fmt)

$dOrderPast25          = $today.AddDays(-25).ToString("yyyy-MM-ddT10:30:00")
$dOrderPast20          = $today.AddDays(-20).ToString("yyyy-MM-ddT14:15:00")
$dOrderYesterdayMorn   = $today.AddDays(-1).ToString("yyyy-MM-ddT09:15:00")
$dOrderYesterdayEve    = $today.AddDays(-1).ToString("yyyy-MM-ddT17:45:00")
$dOrderTodayMorning    = $today.ToString("yyyy-MM-ddT08:30:00")
$dOrderTodayNoon       = $today.ToString("yyyy-MM-ddT12:15:00")
$dOrderTodayAfternoon  = $today.ToString("yyyy-MM-ddT15:20:00")

$productsCsv = @"
id,name,category,unit,price,isDeleted
P00001,ST25 Fragrant Rice 5kg,Grains,Bag,165000.0,false
P00002,Hao Hao Sour Spicy Noodles,Noodles,Pack,4500.0,false
P00003,Australian Beef Sirloin 500g,Meat,Tray,145000.0,false
P00004,Norwegian Salmon Fillet 300g,Seafood,Tray,195000.0,false
P00005,Dalat Hydroponic Lettuce,Produce,Bag,22000.0,false
P00006,Vinamilk Fresh Milk 1L,Dairy,Carton,36000.0,false
P00007,CP Pork & Shrimp Dumplings,Frozen,Pack,68000.0,false
P00008,Three Lady Cooks Canned Tuna,Canned,Can,32000.0,false
P00009,La Vie Mineral Water 1.5L,Beverages,Bottle,24000.0,false
P00010,Nam Ngu Phu Quoc Fish Sauce,Condiments,Bottle,48000.0,false
P00011,Oishi Prawn Crackers 80g,Snacks,Pack,26000.0,false
P00012,ABC Whole Wheat Sandwich Bread,Bakery,Pack,25000.0,false
P00013,Dutch Lady Condensed Milk,Dairy,Can,28000.0,false
P00014,Chinsu Chili Sauce 250g,Condiments,Bottle,18000.0,false
P00015,Discontinued Imported Butter,Dairy,Box,95000.0,true
P00016,Seasonal Lychee Fruit 1kg,Produce,Bag,55000.0,true
"@
Set-Content -Path (Join-Path $dataDir "products.csv") -Value $productsCsv -Encoding UTF8

$batchesCsv = @"
batchId,productId,importDate,productionDate,expirationDate,quantity
B00001,P00001,$dImportPast20,$dProdPast30,$dExpActiveLong,15
B00002,P00001,$dImportPast10,$dProdPast15,$dExpActiveLong,30
B00003,P00002,$dImportPast20,$dProdPast30,$dExpActiveLong,80
B00004,P00002,$dImportPast10,$dProdPast15,$dExpActiveLong,60
B00005,P00003,$dImportPast3,$dProdPast5,$dExpNear1,4
B00006,P00003,$dImportPast10,$dProdPast15,$dExpExpired1,3
B00007,P00004,$dImportPast3,$dProdPast5,$dExpActiveShort,5
B00008,P00005,$dImportPast1,$dProdPast2,$dExpNear2,12
B00009,P00006,$dImportPast10,$dProdPast15,$dExpActiveShort,8
B00010,P00006,$dImportPast3,$dProdPast5,$dExpActiveMed,24
B00011,P00007,$dImportPast20,$dProdPast30,$dExpActiveLong,18
B00012,P00008,$dImportPast20,$dProdPast30,$dExpActiveLong,35
B00013,P00009,$dImportPast20,$dProdPast30,$dExpActiveLong,48
B00014,P00010,$dImportPast20,$dProdPast30,$dExpActiveLong,22
B00015,P00011,$dImportPast20,$dProdPast30,$dExpActiveLong,40
B00016,P00012,$dImportPast10,$dProdPast15,$dExpExpired1,6
B00017,P00012,$dImportPast1,$dProdPast2,$dExpNear1,3
B00018,P00013,$dImportPast20,$dProdPast30,$dExpActiveLong,50
B00019,P00014,$dImportPast45,$dProdPast60,$dExpActiveMed,0
B00020,P00014,$dImportPast3,$dProdPast5,$dExpActiveLong,4
B00021,P00015,$dImportPast45,$dProdPast60,$dExpExpired2,2
B00022,P00016,$dImportPast20,$dProdPast30,$dExpExpired2,10
B00023,P00016,$dImportPast10,$dProdPast15,$dExpExpired1,5
"@
Set-Content -Path (Join-Path $dataDir "batches.csv") -Value $batchesCsv -Encoding UTF8

$customersCsv = @"
id,fullName,phone,address,customerType,isDeleted
C00001,Nguyen Van An,0901234567,"12 Le Loi, Ben Nghe Ward, District 1, Ho Chi Minh City",VIP,false
C00002,Tran Thi Binh,0912345678,"45 Nguyen Hue, District 1, Ho Chi Minh City",Regular,false
C00003,Le Hoang Minh,0987654321,"108 Dien Bien Phu, Ward 6, District 3, Ho Chi Minh City",VIP,false
C00004,Pham Thu Ha,0938112233,"22 Phan Xich Long, Phu Nhuan District, Ho Chi Minh City",Regular,false
C00005,Vo Quoc Bao,0979445566,"88 Nguyen Van Linh, District 7, Ho Chi Minh City",VIP,false
C00006,Dang Minh Tuan,0909887766,"15 Cach Mang Thang 8, District 10, Ho Chi Minh City",Regular,false
C00007,Hoang Gia Huy,0932998877,"30 Ly Tu Trong, District 1, Ho Chi Minh City",Regular,true
C00008,Do Thi Mai,0945667788,"56 Hai Ba Trung, District 1, Ho Chi Minh City",VIP,false
C00009,Bui Quang Khai,0968123456,"72 Vo Thi Sau, District 3, Ho Chi Minh City",Regular,false
C00010,Trinh Hong Ngoc,0918776655,"19 Tran Hung Dao, District 5, Ho Chi Minh City",VIP,true
"@
Set-Content -Path (Join-Path $dataDir "customers.csv") -Value $customersCsv -Encoding UTF8

$ordersCsv = @"
orderId,customerId,orderDate,totalAmount,discount,finalAmount
O00001,C00001,$dOrderPast25,378000.0,37800.0,340200.0
O00002,C00002,$dOrderPast20,117000.0,0.0,117000.0
O00003,C00007,$dOrderPast20,254000.0,0.0,254000.0
O00004,C00003,$dOrderYesterdayMorn,485000.0,48500.0,436500.0
O00005,C00004,$dOrderYesterdayEve,172000.0,0.0,172000.0
O00006,GUEST,$dOrderTodayMorning,124000.0,0.0,124000.0
O00007,C00001,$dOrderTodayMorning,351000.0,35100.0,315900.0
O00008,C00005,$dOrderTodayNoon,194000.0,19400.0,174600.0
O00009,C00008,$dOrderTodayNoon,764000.0,76400.0,687600.0
O00010,C00001,$dOrderTodayAfternoon,303000.0,30300.0,272700.0
"@
Set-Content -Path (Join-Path $dataDir "orders.csv") -Value $ordersCsv -Encoding UTF8

$orderDetailsCsv = @"
orderId,productId,quantity,unitPrice
O00001,P00001,2,165000.0
O00001,P00010,1,48000.0
O00002,P00002,10,4500.0
O00002,P00006,2,36000.0
O00003,P00015,2,95000.0
O00003,P00008,2,32000.0
O00004,P00003,2,145000.0
O00004,P00004,1,195000.0
O00005,P00007,2,68000.0
O00005,P00014,2,18000.0
O00006,P00009,3,24000.0
O00006,P00011,2,26000.0
O00007,P00001,1,165000.0
O00007,P00007,2,68000.0
O00007,P00012,2,25000.0
O00008,P00008,4,32000.0
O00008,P00005,3,22000.0
O00009,P00003,2,145000.0
O00009,P00004,2,195000.0
O00009,P00013,3,28000.0
O00010,P00004,1,195000.0
O00010,P00006,3,36000.0
"@
Set-Content -Path (Join-Path $dataDir "order_details.csv") -Value $orderDetailsCsv -Encoding UTF8

Write-Host " [OK] Seed data successfully generated in $dataDir" -ForegroundColor Green
