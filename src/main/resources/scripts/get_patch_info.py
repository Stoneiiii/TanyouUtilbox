import argparse
from bs4 import BeautifulSoup
import lxml  # BS4使用，这里导入防止打包文件没有导入lxml
import re


def find_cve(html, data_text):
    """
    从html中提取官方补丁信息
    :rtype: object
    :param html: 传入谷歌每月发布Android漏洞页面下载后的html文件
    :param data_text: <1>Framework <2>System <3>Kernel
    :return: 字典文件:{cve编号，对应的url}
    """

    res = {}
    soup = BeautifulSoup(html, 'lxml')
    # target = soup.find(id=data_text)
    target = soup.find(attrs={"data-text": data_text})
    if target is None:
        print("error: type输入错误！")
        return None
    table = target.find_next('table')
    trs = table.find_all('tr')[1:]

    cve_num = len(trs)
    # print("漏洞数量:" + str(cve_num))
    for tr in trs:
        cvename = ""
        url_list = []
        for td in tr:
            # 去除空行
            if td == '\n':
                continue
            # 匹配cvename
            pattern = r'CVE-\d{4}-\d{1,}'
            tmp = re.search(pattern, str(td))
            if tmp:
                cvename = tmp.group()
            # 匹配url
            urls = td.find_all("a")
            if urls:
                for url in urls:
                    url_list.append(str(url['href']))

                    # print(url['href'])
            # print(td)
            # print(url_list)
        if cvename == '' and url_list == []:
            print("error:提取异常！")
            return None
        res[cvename] = url_list  # 写进map
        # print('---------------')
    # print("提取数量:"+str(len(res)))
    if cve_num != len(res):
        print("error:提取数量不匹配异常！")
        return None
    return res


def main(path, type):
    with open(path, 'r') as f:
        html = f.read()
    dic = find_cve(html, type)
    if dic is None:
        return
    for key, value in dic.items():
        print(key + "::" + str(value))


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--path', type=str, default='')
    parser.add_argument('--type', type=str, default='')
    args = parser.parse_args()
    main(args.path, args.type)
