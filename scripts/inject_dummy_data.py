#!/usr/bin/env python3
"""
Inject dummy data into RentLog Room database.
Uses Room's exact generated DDL + identity hash so schema validation passes.
"""
import sqlite3, datetime

DB = r"C:/tmp/rent_log.db"

con = sqlite3.connect(DB)
cur = con.cursor()

# ── Wipe and recreate with Room's exact DDL ────────────────────────────────
cur.executescript("""
DROP TABLE IF EXISTS rent_entries;
DROP TABLE IF EXISTS landlords;
DROP TABLE IF EXISTS room_master_table;
DROP TABLE IF EXISTS android_metadata;

CREATE TABLE IF NOT EXISTS `landlords` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `name` TEXT NOT NULL,
    `tenantName` TEXT NOT NULL,
    `tenantAddress` TEXT NOT NULL,
    `landlordAddress` TEXT NOT NULL,
    `panNumber` TEXT NOT NULL,
    `defaultRentAmount` REAL NOT NULL
);

CREATE TABLE IF NOT EXISTS `rent_entries` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `month` INTEGER NOT NULL,
    `year` INTEGER NOT NULL,
    `amount` REAL NOT NULL,
    `paymentDate` INTEGER NOT NULL,
    `transactionId` TEXT NOT NULL,
    `landlordId` INTEGER NOT NULL,
    `attachmentUri` TEXT NOT NULL,
    FOREIGN KEY(`landlordId`) REFERENCES `landlords`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS `index_rent_entries_landlordId` ON `rent_entries` (`landlordId`);

CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT);
INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES(42, 'e9e2b69a47da9f4e4d80673ac437b888');

CREATE TABLE IF NOT EXISTS android_metadata (locale TEXT);
INSERT INTO android_metadata VALUES('en_US');

PRAGMA user_version = 3;
""")

print("Schema created. Tables:", [r[0] for r in cur.execute(
    "SELECT name FROM sqlite_master WHERE type='table'").fetchall()])

# ── Insert landlord ────────────────────────────────────────────────────────
cur.execute("""
    INSERT INTO landlords (name, tenantName, tenantAddress, landlordAddress, panNumber, defaultRentAmount)
    VALUES (?, ?, ?, ?, ?, ?)
""", (
    "Rajesh Kumar",
    "Aditya Sharma",
    "Flat 201, Green Park Residency, HSR Layout, Bengaluru - 560102",
    "Plot 45, Sector 12, Vasant Vihar, New Delhi - 110057",
    "ABCPK1234D",
    22000.0,
))
landlord_id = cur.lastrowid
print(f"Inserted landlord id={landlord_id}")

# ── Helper ─────────────────────────────────────────────────────────────────
def ts_ms(year, month, day=5):
    dt = datetime.datetime(year, month, day, 10, 0, 0)
    return int(dt.timestamp() * 1000)

# ── FY 2025-26: all 12 months paid ─────────────────────────────────────────
fy2025_entries = [
    (4,  22000.0, ts_ms(2025, 4),  "UPI/250405102300/APR25"),
    (5,  22000.0, ts_ms(2025, 5),  "UPI/250503114500/MAY25"),
    (6,  22000.0, ts_ms(2025, 6),  "UPI/250604123000/JUN25"),
    (7,  22500.0, ts_ms(2025, 7),  "UPI/250705100800/JUL25"),
    (8,  22500.0, ts_ms(2025, 8),  "UPI/250804091700/AUG25"),
    (9,  22500.0, ts_ms(2025, 9),  "UPI/250903114200/SEP25"),
    (10, 22500.0, ts_ms(2025, 10), "UPI/251004105600/OCT25"),
    (11, 22500.0, ts_ms(2025, 11), "UPI/251105084300/NOV25"),
    (12, 22500.0, ts_ms(2025, 12), "UPI/251204113300/DEC25"),
    (1,  22500.0, ts_ms(2026, 1),  "UPI/260104120100/JAN26"),
    (2,  22500.0, ts_ms(2026, 2),  "UPI/260204101800/FEB26"),
    (3,  22500.0, ts_ms(2026, 3),  "UPI/260304095500/MAR26"),
]
for month, amount, pay_date, txn_id in fy2025_entries:
    cur.execute(
        "INSERT INTO rent_entries (month, year, amount, paymentDate, transactionId, landlordId, attachmentUri) VALUES (?,?,?,?,?,?,?)",
        (month, 2025, amount, pay_date, txn_id, landlord_id, "")
    )
print(f"Inserted {len(fy2025_entries)} entries for FY 2025-26")

# ── FY 2026-27: April 2026 (current FY, 1 month) ──────────────────────────
cur.execute(
    "INSERT INTO rent_entries (month, year, amount, paymentDate, transactionId, landlordId, attachmentUri) VALUES (?,?,?,?,?,?,?)",
    (4, 2026, 23000.0, ts_ms(2026, 4, 3), "UPI/260403104700/APR26", landlord_id, "")
)
print("Inserted 1 entry for FY 2026-27")

con.commit()

# ── Verify ─────────────────────────────────────────────────────────────────
count_l = cur.execute("SELECT COUNT(*) FROM landlords").fetchone()[0]
count_r = cur.execute("SELECT COUNT(*) FROM rent_entries").fetchone()[0]
total   = cur.execute("SELECT SUM(amount) FROM rent_entries WHERE year=2025").fetchone()[0]
print(f"\nLandlords: {count_l}  |  RentEntries: {count_r}")
print(f"FY 2025-26 total paid: Rs {total:,.0f}")

con.close()
print("Done.")
