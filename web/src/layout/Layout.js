import React, { Component } from 'react'
import styled from 'styled-components';
import Header from './Header';
import NavPanel from './NavPanel';
import Content from './Content';

const LayoutWrapper = styled.div`
  height: 100%;
  width: 100%;
`

const ContentWrapper = styled.div`
  display: flex;
  height: 100%;
`

export default class Layout extends Component {
  render() {
    return (
      <LayoutWrapper>
        <Header />
        <ContentWrapper>
          <NavPanel />
          <Content />
        </ContentWrapper>
      </LayoutWrapper>
    )
  }
}
