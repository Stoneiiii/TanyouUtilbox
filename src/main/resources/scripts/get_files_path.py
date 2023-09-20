import argparse
import os


def get_xml_path(path):
    """
    获得文件夹下所有文件的详细路径
    :param path:
    :return:
    """
    xml_paths = []
    for dirpath, dirnames, filenames in os.walk(path):
        for filepath in filenames:
            xml_paths.append(os.path.join(dirpath, filepath))
    return xml_paths


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--path', type=str, default='')
    args = parser.parse_args()
    print(get_xml_path(args.path))
