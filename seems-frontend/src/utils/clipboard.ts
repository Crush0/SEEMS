export function copyToClipboard(text: string): Promise<any> {
  return new Promise((resolve, reject) => {
    navigator.clipboard
      .writeText(text)
      .then((result) => {
        resolve(result);
      })
      .catch((err) => {
        reject(err);
      });
  });
}

export function pasteFromClipboard(): Promise<any> {
  return new Promise((resolve, reject) => {
    navigator.clipboard
      .read()
      .then((clipContent) => {
        resolve(clipContent);
      })
      .catch((err) => {
        reject(err);
      });
  });
}
