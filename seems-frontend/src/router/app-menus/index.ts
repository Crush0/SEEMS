import { appExternalRoutes, appRoutes } from '../routes';

const mixinRoutes = [...appRoutes, ...appExternalRoutes];

const appClientMenus = mixinRoutes.map((el) => {
  const { name, path, meta, redirect, children } = el;
  // 过滤掉exception和flow页面(不需要在侧边栏显示的路由可以在这里过滤掉)
  if (
    ['exception', 'Flow', 'list', 'result', 'profile', 'form'].includes(
      name.toString()
    )
  ) {
    return undefined;
  }

  return {
    name,
    path,
    meta,
    redirect,
    children,
  };
});
export default appClientMenus.filter(Boolean);
