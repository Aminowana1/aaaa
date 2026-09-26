"""Check the shaded server artifact, using only the Python standard library."""
import re
import sys
from pathlib import Path
from zipfile import ZipFile


def verify(path: Path) -> None:
    with ZipFile(path) as jar:
        names = set(jar.namelist())
        required = {
            'plugin.yml', 'config.yml', 'messages-v2.yml', 'menus/main.yml', 'menus/create.yml', 'menus/offers.yml',
            'com/mdvcraft/mdveconomy/MDVEconomyPlugin.class',
            'org/sqlite/JDBC.class',
            'com/mdvcraft/commandtree/CommandNode.class',
            'com/mdvcraft/commandtree/CommandTreeManager.class',
            'META-INF/services/java.sql.Driver',
        }
        required.update('menus/' + name + '.yml' for name in (
            'main', 'browse', 'bid', 'trade', 'manage', 'owner', 'offers',
            'create', 'participations', 'claims', 'history'))
        missing = required - names
        if missing:
            raise ValueError(f'Missing required entries: {sorted(missing)}')
        if jar.testzip() is not None:
            raise ValueError('Corrupt JAR entry')
        descriptor = jar.read('plugin.yml').decode('utf-8')
        if '${' in descriptor or not re.search(r'^version:\s*[\'\"]?\S+', descriptor, re.M):
            raise ValueError('plugin.yml version was not resolved by Maven')
        if 'org.sqlite.JDBC' not in jar.read('META-INF/services/java.sql.Driver').decode('utf-8'):
            raise ValueError('SQLite JDBC service descriptor is missing its driver')
        if not any(n.endswith('/libsqlitejdbc.so') for n in names):
            raise ValueError('SQLite Linux native library is missing')
        forbidden = ('org/bukkit/', 'io/papermc/paper/', 'net/milkbowl/vault/',
                     'net/Indyuce/mmoitems/', 'io/lumine/mythic/lib/')
        if any(n.endswith('.class') and n.startswith(forbidden) for n in names):
            raise ValueError('A server-provided API was bundled into the plugin')
        if any(re.fullmatch(r'META-INF/[^/]+\.(SF|RSA|DSA)', n, re.I) for n in names):
            raise ValueError('Dependency signatures must not be retained after shading')
        for name in names:
            if name.startswith('com/mdvcraft/mdveconomy/') and name.endswith('.class'):
                if int.from_bytes(jar.read(name)[6:8], 'big') != 65:
                    raise ValueError(f'Expected Java 21 bytecode: {name}')
    print(f'PASS: shaded JAR verified: {path.name}')


if __name__ == '__main__':
    if len(sys.argv) != 2:
        raise SystemExit('Usage: python scripts/verify_jar.py target/<finalName>.jar')
    verify(Path(sys.argv[1]))
